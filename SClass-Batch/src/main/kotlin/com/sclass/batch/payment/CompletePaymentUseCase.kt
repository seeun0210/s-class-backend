package com.sclass.batch.payment

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.coin.service.CoinDomainService
import com.sclass.domain.domains.payment.adaptor.PaymentAdaptor
import com.sclass.domain.domains.payment.domain.Payment
import com.sclass.domain.domains.payment.domain.PaymentStatus
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CoinProduct
import com.sclass.domain.domains.product.exception.ProductTypeMismatchException
import com.sclass.infrastructure.nicepay.dto.PgInquiryResult
import com.sclass.infrastructure.nicepay.exception.NicePayException
import com.sclass.infrastructure.redis.DistributedLock
import com.sclass.infrastructure.redis.LockKey
import org.slf4j.LoggerFactory
import org.springframework.transaction.support.TransactionTemplate

@UseCase
class CompletePaymentUseCase(
    private val paymentAdaptor: PaymentAdaptor,
    private val productAdaptor: ProductAdaptor,
    private val coinDomainService: CoinDomainService,
    private val txTemplate: TransactionTemplate,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @DistributedLock(prefix = "payment")
    fun execute(
        @LockKey pgOrderId: String,
        payment: Payment,
        result: PgInquiryResult,
    ) {
        // TX1: 상태 확인 + PG 승인/미승인 처리
        val coinContext =
            txTemplate.execute {
                val fresh = paymentAdaptor.findById(payment.id)
                if (fresh.status != PaymentStatus.PENDING) {
                    log.info("이미 처리된 결제 paymentId={}", payment.id)
                    return@execute null
                }

                val tid = result.tid
                if (result.approved && tid != null) {
                    fresh.markPgApproved(tid)
                    val product = productAdaptor.findById(fresh.productId)
                    val coinAmount = (product as? CoinProduct)?.coinAmount ?: throw ProductTypeMismatchException()
                    CoinIssueContext(fresh.userId, coinAmount, fresh.id, product.name)
                } else {
                    fresh.markPgApproveFailed()
                    log.info("결제 미승인 확인 paymentId={}", fresh.id)
                    null
                }
            } ?: return

        // TX2: 코인 발급 + 완료
        try {
            txTemplate.execute {
                val fresh = paymentAdaptor.findById(payment.id)
                coinDomainService.issue(
                    userId = coinContext.userId,
                    amount = coinContext.coinAmount,
                    referenceId = coinContext.paymentId,
                    description = "결제 복구 - ${coinContext.productName}",
                )
                fresh.markCompleted()
            }
            log.info("결제 복구 완료 paymentId={}", payment.id)
        } catch (e: Exception) {
            // TX3: 코인 발급 실패 → ISSUE_COIN_FAILED 마킹
            log.error("결제 복구 중 코인 발급 실패 paymentId={}", payment.id, e)
            txTemplate.execute {
                val fresh = paymentAdaptor.findById(payment.id)
                fresh.markIssueCoinFailed()
            }
        }
    }

    fun handlePgInquiryFailed(
        payment: Payment,
        e: NicePayException,
    ) {
        txTemplate.execute {
            payment.markCompensationNeeded()
            paymentAdaptor.save(payment)
        }
        log.error("NicePay 조회 실패 - 수동 처리 필요 paymentId={}", payment.id, e)
    }

    private data class CoinIssueContext(
        val userId: String,
        val coinAmount: Int,
        val paymentId: String,
        val productName: String,
    )
}
