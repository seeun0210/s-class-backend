package com.sclass.supporters.payment.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.enrollment.domain.Enrollment
import com.sclass.domain.domains.enrollment.domain.EnrollmentStatus
import com.sclass.domain.domains.payment.adaptor.PaymentAdaptor
import com.sclass.domain.domains.payment.domain.PaymentStatus
import com.sclass.domain.domains.payment.exception.PaymentUnauthorizedException
import com.sclass.infrastructure.nicepay.PgGateway
import com.sclass.infrastructure.redis.DistributedLock
import com.sclass.infrastructure.redis.LockKey
import jakarta.transaction.Transactional

@UseCase
class AbandonPaymentLockedUseCase(
    private val paymentAdaptor: PaymentAdaptor,
    private val enrollmentAdaptor: EnrollmentAdaptor,
    private val pgGateway: PgGateway,
) {
    @Transactional
    @DistributedLock(prefix = "payment")
    fun execute(
        userId: String,
        paymentId: String,
        @LockKey pgOrderId: String,
    ) {
        val payment = paymentAdaptor.findById(paymentId)

        if (payment.userId != userId) throw PaymentUnauthorizedException()

        val enrollment = enrollmentAdaptor.findByPaymentIdOrNull(payment.id)

        when (payment.status) {
            PaymentStatus.PENDING -> {
                val inquiryResult = pgGateway.inquiry(pgOrderId)
                if (inquiryResult.approved) {
                    return
                }
                payment.markCancelled()
                paymentAdaptor.save(payment)
                cancelPendingEnrollment(enrollment)
            }

            PaymentStatus.CANCELLED -> {
                cancelPendingEnrollment(enrollment)
            }
            else -> return
        }
    }

    private fun cancelPendingEnrollment(enrollment: Enrollment?) {
        if (enrollment?.status != EnrollmentStatus.PENDING_PAYMENT) {
            return
        }
        enrollment.cancel("사용자 결제 포기")
        enrollmentAdaptor.save(enrollment)
    }
}
