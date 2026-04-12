package com.sclass.supporters.payment.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.coin.service.CoinDomainService
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.payment.adaptor.PaymentAdaptor
import com.sclass.domain.domains.payment.domain.PaymentStatus
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CoinProduct
import com.sclass.domain.domains.product.domain.CourseProduct
import com.sclass.domain.domains.product.exception.ProductTypeMismatchException
import com.sclass.infrastructure.nicepay.PgGateway
import com.sclass.infrastructure.nicepay.exception.NicePayException
import com.sclass.infrastructure.redis.DistributedLock
import com.sclass.infrastructure.redis.LockKey
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.transaction.support.TransactionTemplate

@UseCase
class HandleNicePayReturnUseCase(
    private val paymentAdaptor: PaymentAdaptor,
    private val productAdaptor: ProductAdaptor,
    private val coinDomainService: CoinDomainService,
    private val enrollmentAdaptor: EnrollmentAdaptor,
    private val pgGateway: PgGateway,
    private val txTemplate: TransactionTemplate,
    @param:Value("\${sclass.frontend-url}") private val frontendUrl: String,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @DistributedLock(prefix = "payment")
    fun execute(
        authResultCode: String,
        tid: String?,
        @LockKey orderId: String,
        amount: Int,
        authToken: String?,
        signature: String?,
    ): String {
        if (authResultCode != AUTH_RESULT_SUCCESS) {
            log.warn("NicePay 인증 실패: authResultCode={}, orderId={}", authResultCode, orderId)
            return failureUrl()
        }

        if (authToken == null || signature == null || tid == null) {
            log.warn("NicePay 필수 파라미터 누락: orderId={}", orderId)
            return failureUrl()
        }

        if (!pgGateway.verifyReturnSignature(authToken, amount, signature)) {
            log.warn("NicePay 서명 검증 실패: orderId={}", orderId)
            return failureUrl()
        }

        val payment =
            paymentAdaptor.findByPgOrderIdOrNull(orderId) ?: run {
                log.warn("결제 정보 없음: orderId={}", orderId)
                return failureUrl()
            }

        if (payment.amount != amount) {
            log.warn("결제 금액 불일치: expected={}, actual={}, orderId={}", payment.amount, amount, orderId)
            return failureUrl()
        }

        if (payment.status == PaymentStatus.COMPLETED) {
            log.info("이미 완료된 결제: orderId={}", orderId)
            return successUrl(null)
        }

        if (payment.status != PaymentStatus.PENDING) {
            log.warn("처리 불가 결제 상태: status={}, orderId={}", payment.status, orderId)
            return failureUrl()
        }

        val product = productAdaptor.findById(payment.productId)

        // PG 승인 (외부 API - 트랜잭션 밖)
        val pgResult =
            try {
                pgGateway.approve(payment.pgOrderId, tid, payment.amount)
            } catch (e: NicePayException) {
                log.error("PG 승인 실패: orderId={}", orderId, e)
                txTemplate.execute {
                    val fresh = paymentAdaptor.findById(payment.id)
                    fresh.markPgApproveFailed()
                    paymentAdaptor.save(fresh)
                }
                return failureUrl()
            }

        // TX1: PG 승인 상태 커밋
        txTemplate.execute {
            val fresh = paymentAdaptor.findById(payment.id)
            fresh.markPgApproved(pgResult.tid)
            paymentAdaptor.save(fresh)
        }

        // TX2: 상품 타입별 처리
        return try {
            when (product) {
                is CoinProduct -> {
                    txTemplate.execute {
                        val fresh = paymentAdaptor.findById(payment.id)
                        coinDomainService.issue(
                            userId = fresh.userId,
                            amount = product.coinAmount,
                            referenceId = fresh.id,
                            description = "결제 완료 - ${product.name}",
                        )
                        fresh.markCompleted()
                        paymentAdaptor.save(fresh)
                    }
                    log.info("결제 완료(코인): paymentId={}", payment.id)
                    successUrl(product.coinAmount)
                }
                is CourseProduct -> {
                    txTemplate.execute {
                        val fresh = paymentAdaptor.findById(payment.id)
                        val enrollment = enrollmentAdaptor.findByPaymentId(fresh.id)
                        enrollment.markPaid()
                        enrollmentAdaptor.save(enrollment)
                        fresh.markCompleted()
                        paymentAdaptor.save(fresh)
                    }
                    log.info("결제 완료(수강): paymentId={}", payment.id)
                    successUrl(null)
                }
                else -> throw ProductTypeMismatchException()
            }
        } catch (e: Exception) {
            log.error("결제 후처리 실패: paymentId={}", payment.id, e)
            txTemplate.execute {
                val fresh = paymentAdaptor.findById(payment.id)
                fresh.markIssueCoinFailed()
                paymentAdaptor.save(fresh)
            }
            failureUrl()
        }
    }

    private fun baseUrl() = frontendUrl.trimEnd('/')

    private fun successUrl(issuedCoinAmount: Int?) =
        if (issuedCoinAmount != null) {
            "${baseUrl()}/student/payment/callback?status=COMPLETED&issuedCoinAmount=$issuedCoinAmount"
        } else {
            "${baseUrl()}/student/payment/callback?status=COMPLETED"
        }

    private fun failureUrl() = "${baseUrl()}/student/payment/callback?status=FAILED"

    companion object {
        private const val AUTH_RESULT_SUCCESS = "0000"
    }
}
