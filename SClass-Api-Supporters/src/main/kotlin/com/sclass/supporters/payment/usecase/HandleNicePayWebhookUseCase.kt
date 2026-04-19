package com.sclass.supporters.payment.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.coin.adaptor.CoinPackageAdaptor
import com.sclass.domain.domains.coin.domain.CoinLotSourceType
import com.sclass.domain.domains.coin.service.CoinDomainService
import com.sclass.domain.domains.course.adaptor.CourseAdaptor
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.lesson.service.LessonDomainService
import com.sclass.domain.domains.payment.adaptor.PaymentAdaptor
import com.sclass.domain.domains.payment.domain.PaymentStatus
import com.sclass.domain.domains.payment.domain.PaymentTargetType
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CourseProduct
import com.sclass.domain.domains.product.domain.MembershipProduct
import com.sclass.domain.domains.product.exception.ProductTypeMismatchException
import com.sclass.infrastructure.nicepay.PgGateway
import com.sclass.infrastructure.nicepay.dto.NicePayWebhookPayload
import com.sclass.infrastructure.redis.DistributedLock
import com.sclass.infrastructure.redis.LockKey
import org.slf4j.LoggerFactory
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDateTime

@UseCase
class HandleNicePayWebhookUseCase(
    private val paymentAdaptor: PaymentAdaptor,
    private val productAdaptor: ProductAdaptor,
    private val coinPackageAdaptor: CoinPackageAdaptor,
    private val coinDomainService: CoinDomainService,
    private val pgGateway: PgGateway,
    private val txTemplate: TransactionTemplate,
    private val enrollmentAdaptor: EnrollmentAdaptor,
    private val courseAdaptor: CourseAdaptor,
    private val lessonService: LessonDomainService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @DistributedLock(prefix = "payment")
    fun execute(
        @LockKey orderId: String,
        payload: NicePayWebhookPayload,
    ) {
        if (!pgGateway.verifyWebhookSignature(
                payload.tid,
                payload.amount,
                payload.ediDate,
                payload.signature,
            )
        ) {
            log.warn("웹훅 서명 검증 실패 orderId={}", orderId)
            return
        }

        val payment =
            paymentAdaptor.findByPgOrderIdOrNull(orderId) ?: run {
                log.warn(
                    "웹훅 수신: 알 수 없는 pgOrderId={}",
                    orderId,
                )
                return
            }

        if (payload.resultCode != RESULT_CODE_SUCCESS) {
            log.info(
                "웹훅 수신: 실패 결과 resultCode={}, orderId={}",
                payload.resultCode,
                orderId,
            )
            return
        }

        if (payment.status != PaymentStatus.PENDING) {
            log.info("웹훅 수신: 처리 불필요한 결제 상태 status={}, paymentId={}", payment.status, payment.id)
            return
        }

        when (payment.targetType) {
            PaymentTargetType.COIN_PACKAGE -> {
                val coinPackage = coinPackageAdaptor.findById(payment.targetId)
                txTemplate.execute {
                    val fresh = paymentAdaptor.findById(payment.id)
                    fresh.markPgApproved(payload.tid)
                    paymentAdaptor.save(fresh)
                }
                try {
                    txTemplate.execute {
                        val fresh = paymentAdaptor.findById(payment.id)
                        coinDomainService.issue(
                            userId = fresh.userId,
                            amount = coinPackage.coinAmount,
                            referenceId = fresh.id,
                            description = "결제 완료 (웹훅) - ${coinPackage.name}",
                        )
                        fresh.markCompleted()
                        paymentAdaptor.save(fresh)
                    }
                } catch (e: Exception) {
                    log.error("웹훅 수신: 코인 발급 실패 paymentId={}", payment.id, e)
                    txTemplate.execute {
                        val fresh = paymentAdaptor.findById(payment.id)
                        fresh.markIssueCoinFailed()
                        paymentAdaptor.save(fresh)
                    }
                }
            }
            PaymentTargetType.COURSE_PRODUCT -> {
                val product =
                    productAdaptor.findById(payment.targetId) as? CourseProduct
                        ?: throw ProductTypeMismatchException()
                txTemplate.execute {
                    val fresh = paymentAdaptor.findById(payment.id)
                    fresh.markPgApproved(payload.tid)
                    paymentAdaptor.save(fresh)
                }
                val enrollment = enrollmentAdaptor.findByPaymentIdOrNull(payment.id)
                if (enrollment == null) {
                    log.warn("웹훅 수신: enrollment 없음 paymentId={}", payment.id)
                    return
                }
                txTemplate.execute {
                    val fresh = paymentAdaptor.findById(payment.id)
                    val freshEnrollment = enrollmentAdaptor.findByPaymentId(fresh.id)
                    freshEnrollment.markPaid()
                    enrollmentAdaptor.save(freshEnrollment)
                    fresh.markCompleted()
                    paymentAdaptor.save(fresh)

                    val courseId =
                        freshEnrollment.courseId
                            ?: error("COURSE_PRODUCT enrollment must have courseId")
                    val course = courseAdaptor.findById(courseId)
                    lessonService.createLessonsForEnrollment(
                        freshEnrollment,
                        teacherUserId = course.teacherUserId,
                        courseName = product.name,
                        totalLessons = product.totalLessons,
                    )
                }
            }
            PaymentTargetType.MEMBERSHIP_PRODUCT -> {
                val product =
                    productAdaptor.findById(payment.targetId) as? MembershipProduct
                        ?: throw ProductTypeMismatchException()
                val coinPackage = coinPackageAdaptor.findById(product.coinPackageId)
                txTemplate.execute {
                    val fresh = paymentAdaptor.findById(payment.id)
                    fresh.markPgApproved(payload.tid)
                    paymentAdaptor.save(fresh)
                }
                val enrollment = enrollmentAdaptor.findByPaymentIdOrNull(payment.id)
                if (enrollment == null) {
                    log.warn("웹훅 수신: enrollment 없음 paymentId={}", payment.id)
                    return
                }
                try {
                    txTemplate.execute {
                        val fresh = paymentAdaptor.findById(payment.id)
                        val freshEnrollment = enrollmentAdaptor.findByPaymentId(fresh.id)
                        val now = LocalDateTime.now()
                        val endAt = now.plusDays(product.periodDays.toLong())
                        freshEnrollment.markPaid(startAt = now, endAt = endAt)
                        enrollmentAdaptor.save(freshEnrollment)

                        coinDomainService.issue(
                            userId = fresh.userId,
                            amount = coinPackage.coinAmount,
                            referenceId = fresh.id,
                            description = "멤버십 가입 (웹훅) - ${product.name}",
                            enrollmentId = freshEnrollment.id,
                            expireAt = endAt,
                            sourceType = CoinLotSourceType.PURCHASE,
                            sourceMeta = """{"membershipProductId":"${product.id}"}""",
                        )
                        fresh.markCompleted()
                        paymentAdaptor.save(fresh)
                    }
                } catch (e: Exception) {
                    log.error("웹훅 수신: 멤버십 발급 실패 paymentId={}", payment.id, e)
                    txTemplate.execute {
                        val fresh = paymentAdaptor.findById(payment.id)
                        fresh.markIssueCoinFailed()
                        paymentAdaptor.save(fresh)
                    }
                }
            }
        }
    }

    companion object {
        private const val RESULT_CODE_SUCCESS = "0000"
    }
}
