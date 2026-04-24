package com.sclass.supporters.payment.usecase

import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.enrollment.domain.Enrollment
import com.sclass.domain.domains.payment.adaptor.PaymentAdaptor
import com.sclass.domain.domains.payment.domain.Payment
import com.sclass.domain.domains.payment.domain.PaymentStatus
import com.sclass.domain.domains.payment.domain.PaymentTargetType
import com.sclass.domain.domains.payment.domain.PgType
import com.sclass.domain.domains.payment.exception.PaymentUnauthorizedException
import com.sclass.infrastructure.nicepay.PgGateway
import com.sclass.infrastructure.nicepay.dto.PgInquiryResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AbandonPaymentLockedUseCaseTest {
    private lateinit var paymentAdaptor: PaymentAdaptor
    private lateinit var enrollmentAdaptor: EnrollmentAdaptor
    private lateinit var pgGateway: PgGateway
    private lateinit var useCase: AbandonPaymentLockedUseCase

    @BeforeEach
    fun setUp() {
        paymentAdaptor = mockk()
        enrollmentAdaptor = mockk()
        pgGateway = mockk()
        useCase = AbandonPaymentLockedUseCase(paymentAdaptor, enrollmentAdaptor, pgGateway)
    }

    private fun pendingPayment(userId: String = "user-id-1") =
        Payment(
            userId = userId,
            targetType = PaymentTargetType.COURSE_PRODUCT,
            targetId = "product-id-1",
            amount = 300000,
            pgType = PgType.NICEPAY,
            pgOrderId = "order-id-1",
        )

    @Test
    fun `PENDING payment abandon 시 payment와 enrollment를 함께 취소한다`() {
        val payment = pendingPayment()
        val enrollment =
            Enrollment.createForPurchase(
                productId = "product-id-1",
                studentUserId = payment.userId,
                tuitionAmountWon = 300000,
                paymentId = payment.id,
                courseId = 1L,
            )

        every { paymentAdaptor.findById(payment.id) } returns payment
        every { enrollmentAdaptor.findByPaymentIdOrNull(payment.id) } returns enrollment
        every { pgGateway.inquiry(payment.pgOrderId) } returns PgInquiryResult(approved = false, tid = null, amount = payment.amount)
        every { paymentAdaptor.save(any()) } answers { firstArg() }
        every { enrollmentAdaptor.save(any()) } answers { firstArg() }

        useCase.execute(payment.userId, payment.id, payment.pgOrderId)

        assertThat(payment.status).isEqualTo(PaymentStatus.CANCELLED)
        assertThat(enrollment.status.name).isEqualTo("CANCELLED")
        assertThat(enrollment.cancelReason).isEqualTo("사용자 결제 포기")
        verify(exactly = 1) { paymentAdaptor.save(payment) }
        verify(exactly = 1) { enrollmentAdaptor.save(enrollment) }
    }

    @Test
    fun `PG 조회 결과 이미 승인된 거래면 abandon 처리하지 않는다`() {
        val payment = pendingPayment()

        every { paymentAdaptor.findById(payment.id) } returns payment
        every { enrollmentAdaptor.findByPaymentIdOrNull(payment.id) } returns null
        every { pgGateway.inquiry(payment.pgOrderId) } returns PgInquiryResult(approved = true, tid = "tid-1", amount = payment.amount)

        useCase.execute(payment.userId, payment.id, payment.pgOrderId)

        assertThat(payment.status).isEqualTo(PaymentStatus.PENDING)
        verify(exactly = 0) { paymentAdaptor.save(any()) }
        verify(exactly = 0) { enrollmentAdaptor.save(any()) }
    }

    @Test
    fun `이미 CANCELLED면 idempotent 하게 종료한다`() {
        val payment = pendingPayment().apply { markCancelled() }

        every { paymentAdaptor.findById(payment.id) } returns payment
        every { enrollmentAdaptor.findByPaymentIdOrNull(payment.id) } returns null

        useCase.execute(payment.userId, payment.id, payment.pgOrderId)

        verify(exactly = 0) { paymentAdaptor.save(any()) }
        verify(exactly = 0) { enrollmentAdaptor.save(any()) }
    }

    @Test
    fun `PG_APPROVED 이후 상태면 아무 작업도 하지 않는다`() {
        val payment = pendingPayment().apply { markPgApproved("tid-1") }

        every { paymentAdaptor.findById(payment.id) } returns payment
        every { enrollmentAdaptor.findByPaymentIdOrNull(payment.id) } returns null

        useCase.execute(payment.userId, payment.id, payment.pgOrderId)

        verify(exactly = 0) { paymentAdaptor.save(any()) }
        verify(exactly = 0) { enrollmentAdaptor.save(any()) }
    }

    @Test
    fun `다른 사용자 결제면 PaymentUnauthorizedException이 발생한다`() {
        val payment = pendingPayment(userId = "owner-id")

        every { paymentAdaptor.findById(payment.id) } returns payment

        assertThatThrownBy {
            useCase.execute("other-user-id", payment.id, payment.pgOrderId)
        }.isInstanceOf(PaymentUnauthorizedException::class.java)
    }

    @Test
    fun `coin package payment도 payment만 취소할 수 있다`() {
        val payment =
            Payment(
                userId = "user-id-1",
                targetType = PaymentTargetType.COIN_PACKAGE,
                targetId = "coin-package-id-1",
                amount = 10000,
                pgType = PgType.NICEPAY,
                pgOrderId = "order-id-2",
            )

        every { paymentAdaptor.findById(payment.id) } returns payment
        every { enrollmentAdaptor.findByPaymentIdOrNull(payment.id) } returns null
        every { pgGateway.inquiry(payment.pgOrderId) } returns PgInquiryResult(approved = false, tid = null, amount = payment.amount)
        every { paymentAdaptor.save(any()) } answers { firstArg() }

        useCase.execute(payment.userId, payment.id, payment.pgOrderId)

        assertThat(payment.status).isEqualTo(PaymentStatus.CANCELLED)
        verify(exactly = 1) { paymentAdaptor.save(payment) }
    }
}
