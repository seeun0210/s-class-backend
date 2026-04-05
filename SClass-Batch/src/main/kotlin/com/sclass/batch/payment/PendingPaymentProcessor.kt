package com.sclass.batch.payment

import com.sclass.domain.domains.coin.service.CoinDomainService
import com.sclass.domain.domains.payment.adaptor.PaymentAdaptor
import com.sclass.domain.domains.payment.domain.Payment
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CoinProduct
import com.sclass.infrastructure.nicepay.PgGateway
import com.sclass.infrastructure.nicepay.exception.NicePayException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class PendingPaymentProcessor(
    private val paymentAdaptor: PaymentAdaptor,
    private val productAdaptor: ProductAdaptor,
    private val coinDomainService: CoinDomainService,
    private val pgGateway: PgGateway,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun process(payment: Payment) {
        try {
            val result = pgGateway.inquiry(payment.pgOrderId)

            val tid = result.tid

            if (result.approved && tid != null) {
                payment.markPgApproved(tid)

                val product =
                    productAdaptor.findById(payment.productId)
                val coinAmount = (product as CoinProduct).coinAmount

                coinDomainService.issue(
                    userId = payment.userId,
                    amount = coinAmount,
                    referenceId = payment.id,
                    description = "결제 복구 - ${product.name}",
                )

                payment.markCompleted()
                log.info("결제 복구 완료 paymentId={}", payment.id)
            } else {
                payment.markPgApproveFailed()
                log.info("결제 미승인 확인 paymentId={}", payment.id)
            }
        } catch (e: NicePayException) {
            payment.markCompensationNeeded()
            log.error("NicePay 조회 실패 - 수동 처리 필요 paymentId={}", payment.id, e)
        }

        paymentAdaptor.save(payment)
    }
}
