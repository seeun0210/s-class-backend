package com.sclass.supporters.enrollment.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.common.vo.Ulid
import com.sclass.domain.domains.course.adaptor.CourseAdaptor
import com.sclass.domain.domains.course.exception.CourseNotEnrollableException
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.enrollment.domain.Enrollment
import com.sclass.domain.domains.enrollment.exception.EnrollmentInvalidPurchaseTargetException
import com.sclass.domain.domains.payment.adaptor.PaymentAdaptor
import com.sclass.domain.domains.payment.domain.Payment
import com.sclass.domain.domains.payment.domain.PaymentTargetType
import com.sclass.domain.domains.payment.domain.PgType
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CourseProduct
import com.sclass.domain.domains.product.exception.ProductTypeMismatchException
import com.sclass.infrastructure.redis.DistributedLock
import com.sclass.infrastructure.redis.LockKey
import com.sclass.supporters.enrollment.dto.PrepareEnrollmentResponse
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@UseCase
class PrepareEnrollmentUseCase(
    private val courseAdaptor: CourseAdaptor,
    private val productAdaptor: ProductAdaptor,
    private val enrollmentAdaptor: EnrollmentAdaptor,
    private val paymentAdaptor: PaymentAdaptor,
) {
    @Transactional
    @DistributedLock(prefix = "enrollment")
    fun execute(
        studentUserId: String,
        @LockKey productId: String,
        courseId: Long?,
        pgType: PgType,
    ): PrepareEnrollmentResponse {
        val product =
            productAdaptor.findById(productId) as? CourseProduct
                ?: throw ProductTypeMismatchException()

        return if (product.matchingEnabled) {
            prepareMatchingEnrollment(
                studentUserId = studentUserId,
                productId = productId,
                product = product,
                courseId = courseId,
                pgType = pgType,
            )
        } else {
            prepareRegularEnrollment(
                studentUserId = studentUserId,
                productId = productId,
                product = product,
                courseId = courseId,
                pgType = pgType,
            )
        }
    }

    private fun prepareMatchingEnrollment(
        studentUserId: String,
        productId: String,
        product: CourseProduct,
        courseId: Long?,
        pgType: PgType,
    ): PrepareEnrollmentResponse {
        if (courseId != null) throw EnrollmentInvalidPurchaseTargetException()

        enrollmentAdaptor.findResumableCourseProductEnrollment(productId, studentUserId)?.let { live ->
            val payment = paymentAdaptor.findById(live.paymentId!!)
            return PrepareEnrollmentResponse(
                paymentId = payment.id,
                pgOrderId = payment.pgOrderId,
                amount = payment.amount,
                productId = productId,
                courseId = null,
                courseName = product.name,
            )
        }

        val payment =
            paymentAdaptor.save(
                Payment(
                    userId = studentUserId,
                    targetType = PaymentTargetType.COURSE_PRODUCT,
                    targetId = productId,
                    amount = product.priceWon,
                    pgType = pgType,
                    pgOrderId = Ulid.generate(),
                ),
            )

        enrollmentAdaptor.save(
            Enrollment.createForPurchase(
                productId = productId,
                courseId = null,
                studentUserId = studentUserId,
                tuitionAmountWon = product.priceWon,
                paymentId = payment.id,
            ),
        )

        return PrepareEnrollmentResponse(
            paymentId = payment.id,
            pgOrderId = payment.pgOrderId,
            amount = payment.amount,
            productId = productId,
            courseId = null,
            courseName = product.name,
        )
    }

    private fun prepareRegularEnrollment(
        studentUserId: String,
        productId: String,
        product: CourseProduct,
        courseId: Long?,
        pgType: PgType,
    ): PrepareEnrollmentResponse {
        val resolvedCourseId = courseId ?: throw EnrollmentInvalidPurchaseTargetException()
        val course = courseAdaptor.findById(resolvedCourseId)

        if (course.productId != productId) throw EnrollmentInvalidPurchaseTargetException()

        enrollmentAdaptor.findResumableEnrollment(resolvedCourseId, studentUserId)?.let { live ->
            val payment = paymentAdaptor.findById(live.paymentId!!)
            return PrepareEnrollmentResponse(
                payment.id,
                payment.pgOrderId,
                payment.amount,
                productId,
                course.id,
                product.name,
            )
        }

        val liveCount = enrollmentAdaptor.countLiveEnrollments(resolvedCourseId)
        if (!course.canEnroll(LocalDateTime.now(), liveCount)) {
            throw CourseNotEnrollableException()
        }

        val payment =
            paymentAdaptor.save(
                Payment(
                    userId = studentUserId,
                    targetType = PaymentTargetType.COURSE_PRODUCT,
                    targetId = productId,
                    amount = product.priceWon,
                    pgType = pgType,
                    pgOrderId = Ulid.generate(),
                ),
            )

        enrollmentAdaptor.save(
            Enrollment.createForPurchase(
                productId = productId,
                courseId = course.id,
                studentUserId = studentUserId,
                tuitionAmountWon = product.priceWon,
                paymentId = payment.id,
            ),
        )

        return PrepareEnrollmentResponse(
            paymentId = payment.id,
            pgOrderId = payment.pgOrderId,
            amount = payment.amount,
            productId = productId,
            courseId = course.id,
            courseName = product.name,
        )
    }
}
