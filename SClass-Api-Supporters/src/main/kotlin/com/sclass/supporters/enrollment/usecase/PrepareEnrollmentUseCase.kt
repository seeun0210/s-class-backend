package com.sclass.supporters.enrollment.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.common.vo.Ulid
import com.sclass.domain.domains.course.adaptor.CourseAdaptor
import com.sclass.domain.domains.course.exception.CourseNotEnrollableException
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.enrollment.domain.Enrollment
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
        @LockKey courseId: Long,
        pgType: PgType,
    ): PrepareEnrollmentResponse {
        val course = courseAdaptor.findById(courseId)
        val product =
            productAdaptor.findById(course.productId) as? CourseProduct
                ?: throw ProductTypeMismatchException()

        enrollmentAdaptor.findResumableEnrollment(courseId, studentUserId)?.let { live ->
            val payment = paymentAdaptor.findById(live.paymentId!!)
            return PrepareEnrollmentResponse(payment.id, payment.pgOrderId, payment.amount, course.id, product.name)
        }

        val liveCount = enrollmentAdaptor.countLiveEnrollments(courseId)
        if (!course.canEnroll(LocalDateTime.now(), liveCount)) {
            throw CourseNotEnrollableException()
        }

        val payment =
            paymentAdaptor.save(
                Payment(
                    userId = studentUserId,
                    targetType = PaymentTargetType.COURSE_PRODUCT,
                    targetId = product.id,
                    amount = product.priceWon,
                    pgType = pgType,
                    pgOrderId = Ulid.generate(),
                ),
            )

        enrollmentAdaptor.save(
            Enrollment.createForPurchase(
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
            courseId = course.id,
            courseName = product.name,
        )
    }
}
