package com.sclass.domain.domains.enrollment.domain

import com.sclass.domain.domains.enrollment.exception.EnrollmentInvalidStatusTransitionException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class EnrollmentTest {
    @Test
    fun `assignCourse는 PENDING_PAYMENT enrollment에 대해 실패한다`() {
        val enrollment =
            Enrollment.createForPurchase(
                productId = "product-id-00000000001",
                studentUserId = "student-id-00000000001",
                tuitionAmountWon = 300000,
                paymentId = "payment-id-000000000001",
            )

        assertThatThrownBy { enrollment.assignCourse(1L) }
            .isInstanceOf(EnrollmentInvalidStatusTransitionException::class.java)

        assertThat(enrollment.courseId).isNull()
        assertThat(enrollment.status).isEqualTo(EnrollmentStatus.PENDING_PAYMENT)
    }

    @Test
    fun `assignCourse는 PENDING_MATCH enrollment에 대해 ACTIVE로 전이한다`() {
        val enrollment =
            Enrollment.createForPurchase(
                productId = "product-id-00000000001",
                studentUserId = "student-id-00000000001",
                tuitionAmountWon = 300000,
                paymentId = "payment-id-000000000001",
            )

        enrollment.markPendingMatch()
        enrollment.assignCourse(1L)

        assertThat(enrollment.courseId).isEqualTo(1L)
        assertThat(enrollment.status).isEqualTo(EnrollmentStatus.ACTIVE)
    }
}
