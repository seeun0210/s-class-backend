package com.sclass.supporters.payment.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.coin.adaptor.CoinPackageAdaptor
import com.sclass.domain.domains.coin.domain.CoinLotSourceType
import com.sclass.domain.domains.coin.service.CoinDomainService
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.enrollment.exception.EnrollmentCourseRequiredException
import com.sclass.domain.domains.payment.adaptor.PaymentAdaptor
import com.sclass.domain.domains.payment.domain.Payment
import com.sclass.domain.domains.payment.domain.PaymentCancelSource
import com.sclass.domain.domains.payment.domain.PaymentStatus
import com.sclass.domain.domains.payment.domain.PaymentTargetType
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CourseProduct
import com.sclass.domain.domains.product.domain.MembershipProduct
import com.sclass.domain.domains.product.exception.ProductTypeMismatchException
import com.sclass.infrastructure.nicepay.PgGateway
import com.sclass.infrastructure.nicepay.exception.NicePayException
import com.sclass.infrastructure.redis.DistributedLock
import com.sclass.infrastructure.redis.LockKey
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDateTime

@UseCase
class HandleNicePayReturnUseCase(
    private val paymentAdaptor: PaymentAdaptor,
    private val productAdaptor: ProductAdaptor,
    private val coinPackageAdaptor: CoinPackageAdaptor,
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

        val pendingCancelSource = payment.pendingCancelSource()
        if (payment.status == PaymentStatus.CANCELLED && pendingCancelSource != null) {
            compensateApprovedCancelledPayment(payment, tid, pendingCancelSource)
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

        // TX2: 결제 대상별 처리
        return try {
            when (payment.targetType) {
                PaymentTargetType.COIN_PACKAGE -> {
                    val coinPackage = coinPackageAdaptor.findById(payment.targetId)
                    txTemplate.execute {
                        val fresh = paymentAdaptor.findById(payment.id)
                        coinDomainService.issue(
                            userId = fresh.userId,
                            amount = coinPackage.coinAmount,
                            referenceId = fresh.id,
                            description = "결제 완료 - ${coinPackage.name}",
                        )
                        fresh.markCompleted()
                        paymentAdaptor.save(fresh)
                    }
                    log.info("결제 완료(코인): paymentId={}", payment.id)
                    successUrl(coinPackage.coinAmount)
                }
                PaymentTargetType.COURSE_PRODUCT -> {
                    val product =
                        productAdaptor.findById(payment.targetId) as? CourseProduct
                            ?: throw ProductTypeMismatchException()
                    txTemplate.execute {
                        val fresh = paymentAdaptor.findById(payment.id)
                        val enrollment = enrollmentAdaptor.findByPaymentId(fresh.id)
                        if (product.requiresMatching) {
                            enrollment.markPendingMatch()
                        } else {
                            if (enrollment.courseId == null) throw EnrollmentCourseRequiredException()
                            enrollment.markCoursePaid()
                        }
                        enrollmentAdaptor.save(enrollment)
                        fresh.markCompleted()
                        paymentAdaptor.save(fresh)
                    }
                    log.info("결제 완료(수강): paymentId={}", payment.id)
                    successUrl(null)
                }
                PaymentTargetType.MEMBERSHIP_PRODUCT -> {
                    val product =
                        productAdaptor.findById(payment.targetId) as? MembershipProduct
                            ?: throw ProductTypeMismatchException()
                    val coinPackage = coinPackageAdaptor.findById(product.coinPackageId)
                    txTemplate.execute {
                        val fresh = paymentAdaptor.findById(payment.id)
                        val enrollment = enrollmentAdaptor.findByPaymentId(fresh.id)
                        val now = LocalDateTime.now()
                        val period = product.resolveActivePeriod(now)
                        enrollment.markMembershipPaid(startAt = period.startAt, endAt = period.endAt)
                        enrollmentAdaptor.save(enrollment)

                        coinDomainService.issue(
                            userId = fresh.userId,
                            amount = coinPackage.coinAmount,
                            referenceId = fresh.id,
                            description = "멤버십 가입 - ${product.name}",
                            enrollmentId = enrollment.id,
                            expireAt = period.endAt,
                            sourceType = CoinLotSourceType.PURCHASE,
                            sourceMeta = """{"membershipProductId":"${product.id}"}""",
                        )
                        fresh.markCompleted()
                        paymentAdaptor.save(fresh)
                    }
                    log.info("결제 완료(멤버십): paymentId={}", payment.id)
                    successUrl(coinPackage.coinAmount)
                }
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

    private fun compensateApprovedCancelledPayment(
        payment: Payment,
        tid: String,
        cancelSource: PaymentCancelSource,
    ) {
        val cancelSuccess =
            runCatching { pgGateway.cancel(tid, payment.amount, cancelSource.compensationReason()) }
                .onFailure { e -> log.error("자동 승인취소 실패: orderId={}", payment.pgOrderId, e) }
                .isSuccess

        txTemplate.execute {
            val fresh = paymentAdaptor.findById(payment.id)
            if (cancelSuccess) {
                fresh.markCancelCompensated(cancelSource)
            } else {
                fresh.markPgCancelFailed(cancelSource)
            }
            paymentAdaptor.save(fresh)
        }
    }

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
