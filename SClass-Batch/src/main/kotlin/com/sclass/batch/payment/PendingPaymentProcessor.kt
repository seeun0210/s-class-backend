package com.sclass.batch.payment

import com.sclass.domain.domains.coin.service.CoinDomainService
import com.sclass.domain.domains.payment.adaptor.PaymentAdaptor
import com.sclass.domain.domains.payment.domain.Payment
import com.sclass.domain.domains.payment.domain.PaymentStatus
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CoinProduct
import com.sclass.domain.domains.product.exception.ProductTypeMismatchException
import com.sclass.infrastructure.nicepay.PgGateway
import com.sclass.infrastructure.nicepay.dto.PgInquiryResult
import com.sclass.infrastructure.nicepay.exception.NicePayException
import com.sclass.infrastructure.redis.DistributedLock
import com.sclass.infrastructure.redis.LockKey
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class PendingPaymentProcessor(
    private val paymentAdaptor: PaymentAdaptor,
    private val productAdaptor: ProductAdaptor,
    private val coinDomainService: CoinDomainService,
    private val pgGateway: PgGateway,
    @Lazy private val self: PendingPaymentProcessor,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun process(payment: Payment) {
        // PG 조회는 트랜잭션/락 밖에서 (외부 API)
        val result =
            try {
                pgGateway.inquiry(payment.pgOrderId)
            } catch (e: NicePayException) {
                self.handlePgInquiryFailed(payment, e)
                return
            }

        // DB 변경은 락 안에서 (프록시를 통해 호출하여 AOP 적용)
        self.completePayment(payment.pgOrderId, payment, result)
    }

    @DistributedLock(prefix = "payment")
    @Transactional
    fun completePayment(
        @LockKey pgOrderId: String,
        payment: Payment,
        result: PgInquiryResult,
    ) {
        // 락 안에서 상태 다시 확인 (Return/Webhook이 이미 처리했을 수 있음)
        val freshPayment = paymentAdaptor.findById(payment.id)
        if (freshPayment.status != PaymentStatus.PENDING) {
            log.info("이미 처리된 결제 paymentId={}", payment.id)
            return
        }

        val tid = result.tid
        if (result.approved && tid != null) {
            freshPayment.markPgApproved(tid)
            val product = productAdaptor.findById(freshPayment.productId)
            val coinAmount = (product as? CoinProduct)?.coinAmount ?: throw ProductTypeMismatchException()
            coinDomainService.issue(
                userId = freshPayment.userId,
                amount = coinAmount,
                referenceId = freshPayment.id,
                description = "결제 복구 - ${product.name}",
            )
            freshPayment.markCompleted()
            log.info("결제 복구 완료 paymentId={}", freshPayment.id)
        } else {
            freshPayment.markPgApproveFailed()
            log.info("결제 미승인 확인 paymentId={}", freshPayment.id)
        }
    }

    @Transactional
    fun handlePgInquiryFailed(
        payment: Payment,
        e: NicePayException,
    ) {
        payment.markCompensationNeeded()
        paymentAdaptor.save(payment)
        log.error("NicePay 조회 실패 - 수동 처리 필요 paymentId={}", payment.id, e)
    }
}
