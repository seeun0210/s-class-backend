package com.sclass.supporters.enrollment.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.common.vo.Ulid
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.enrollment.domain.Enrollment
import com.sclass.domain.domains.enrollment.exception.EnrollmentInvalidPurchaseTargetException
import com.sclass.domain.domains.payment.adaptor.PaymentAdaptor
import com.sclass.domain.domains.payment.domain.Payment
import com.sclass.domain.domains.payment.domain.PaymentTargetType
import com.sclass.domain.domains.payment.domain.PgType
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.infrastructure.redis.DistributedLock
import com.sclass.infrastructure.redis.LockKey
import com.sclass.supporters.enrollment.dto.PrepareEnrollmentResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class PrepareMatchingEnrollmentUseCase(
    private val productAdaptor: ProductAdaptor,
    private val enrollmentAdaptor: EnrollmentAdaptor,
    private val paymentAdaptor: PaymentAdaptor,
) {
    @Transactional
    @DistributedLock(prefix = "course-product", waitTime = 30)
    fun execute(
        @LockKey productId: String,
        studentUserId: String,
        courseId: Long?,
        pgType: PgType,
    ): PrepareEnrollmentResponse {
        val product = productAdaptor.findCourseProductById(productId)
        if (!product.requiresMatching) throw EnrollmentInvalidPurchaseTargetException()
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
}
