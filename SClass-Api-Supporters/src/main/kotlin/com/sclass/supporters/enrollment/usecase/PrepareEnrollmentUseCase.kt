package com.sclass.supporters.enrollment.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.common.vo.Ulid
import com.sclass.domain.domains.course.adaptor.CourseAdaptor
import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.domain.domains.course.exception.CourseNotActiveException
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.enrollment.domain.Enrollment
import com.sclass.domain.domains.enrollment.exception.EnrollmentAlreadyExistsException
import com.sclass.domain.domains.payment.adaptor.PaymentAdaptor
import com.sclass.domain.domains.payment.domain.Payment
import com.sclass.domain.domains.payment.domain.PgType
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CourseProduct
import com.sclass.domain.domains.product.exception.ProductTypeMismatchException
import com.sclass.infrastructure.redis.DistributedLock
import com.sclass.infrastructure.redis.LockKey
import com.sclass.supporters.enrollment.dto.PrepareEnrollmentResponse
import org.springframework.transaction.annotation.Transactional

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
        @LockKey studentUserId: String,
        @LockKey courseId: Long,
        pgType: PgType,
    ): PrepareEnrollmentResponse {
        val course = courseAdaptor.findById(courseId)
        if (course.status != CourseStatus.ACTIVE) throw CourseNotActiveException()

        val product =
            productAdaptor.findById(course.productId) as? CourseProduct
                ?: throw ProductTypeMismatchException()

        if (enrollmentAdaptor.findLiveEnrollment(courseId, studentUserId) != null) {
            throw EnrollmentAlreadyExistsException()
        }

        val payment =
            paymentAdaptor.save(
                Payment(
                    userId = studentUserId,
                    productId = product.id,
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
                teacherPayoutPerLessonWon = product.teacherPayoutPerLessonWon,
                paymentId = payment.id,
            ),
        )

        return PrepareEnrollmentResponse(
            paymentId = payment.id,
            pgOrderId = payment.pgOrderId,
            amount = payment.amount,
            courseId = course.id,
            courseName = course.name,
        )
    }
}
