package com.sclass.batch.enrollment

import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.enrollment.domain.Enrollment
import com.sclass.domain.domains.enrollment.domain.EnrollmentStatus
import com.sclass.domain.domains.payment.adaptor.PaymentAdaptor
import com.sclass.domain.domains.payment.domain.Payment
import com.sclass.domain.domains.payment.domain.PaymentStatus
import com.sclass.domain.domains.payment.domain.PaymentTargetType
import com.sclass.domain.domains.payment.domain.PgType
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionCallback
import org.springframework.transaction.support.TransactionTemplate
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class CleanupStalePendingEnrollmentsJobTest {
    private val enrollmentAdaptor = mockk<EnrollmentAdaptor>()
    private val paymentAdaptor = mockk<PaymentAdaptor>()
    private val txTemplate = mockk<TransactionTemplate>()
    private val fixedClock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneId.of("UTC"))
    private val job = CleanupStalePendingEnrollmentsJob(enrollmentAdaptor, paymentAdaptor, txTemplate, fixedClock)

    @BeforeEach
    fun setUp() {
        every { txTemplate.execute<Any?>(any()) } answers {
            firstArg<TransactionCallback<Any?>>().doInTransaction(mockk<TransactionStatus>())
        }
    }

    private fun createPendingEnrollment(paymentId: String = "pay0000000000000000001") =
        Enrollment.createForPurchase(
            courseId = 1L,
            studentUserId = "user000000000000000001",
            tuitionAmountWon = 100_000,
            paymentId = paymentId,
        )

    private fun createPayment(id: String = "pay0000000000000000001") =
        Payment(
            id = id,
            userId = "user000000000000000001",
            targetType = PaymentTargetType.COURSE_PRODUCT,
            targetId = "prod000000000000000001",
            amount = 100_000,
            pgType = PgType.NICEPAY,
            pgOrderId = "order-$id",
        )

    @Test
    fun `만료된 PENDING_PAYMENT enrollment가 없으면 아무것도 처리하지 않는다`() {
        every { enrollmentAdaptor.findPendingPaymentOlderThan(any()) } returns emptyList()

        job.execute()

        verify(exactly = 0) { paymentAdaptor.findById(any()) }
        verify(exactly = 0) { enrollmentAdaptor.save(any()) }
    }

    @Test
    fun `만료된 PENDING_PAYMENT enrollment와 연결된 Payment를 모두 CANCELLED로 변경한다`() {
        val enrollment = createPendingEnrollment()
        val payment = createPayment()

        every { enrollmentAdaptor.findPendingPaymentOlderThan(any()) } returns listOf(enrollment)
        every { enrollmentAdaptor.findById(enrollment.id) } returns enrollment
        every { paymentAdaptor.findById(payment.id) } returns payment
        every { enrollmentAdaptor.save(any()) } returns enrollment
        every { paymentAdaptor.save(any()) } returns payment

        job.execute()

        assertAll(
            { assertEquals(EnrollmentStatus.CANCELLED, enrollment.status) },
            { assertEquals(PaymentStatus.CANCELLED, payment.status) },
        )
        verify { enrollmentAdaptor.save(enrollment) }
        verify { paymentAdaptor.save(payment) }
    }

    @Test
    fun `enrollment가 이미 PENDING_PAYMENT가 아니면 건너뛴다`() {
        val staleRef = createPendingEnrollment()
        val alreadyActive =
            Enrollment.createByGrant(
                courseId = 1L,
                studentUserId = "user000000000000000001",
                grantedByUserId = "admin00000000000000001",
                grantReason = "테스트",
            )

        every { enrollmentAdaptor.findPendingPaymentOlderThan(any()) } returns listOf(staleRef)
        every { enrollmentAdaptor.findById(staleRef.id) } returns alreadyActive

        job.execute()

        verify(exactly = 0) { paymentAdaptor.findById(any()) }
        verify(exactly = 0) { enrollmentAdaptor.save(any()) }
    }

    @Test
    fun `payment가 PG_APPROVED 상태이면 enrollment를 취소하지 않고 건너뛴다`() {
        val enrollment = createPendingEnrollment()
        val payment = createPayment().also { it.markPgApproved("tid-001") }

        every { enrollmentAdaptor.findPendingPaymentOlderThan(any()) } returns listOf(enrollment)
        every { enrollmentAdaptor.findById(enrollment.id) } returns enrollment
        every { paymentAdaptor.findById(payment.id) } returns payment

        job.execute()

        assertAll(
            { assertEquals(EnrollmentStatus.PENDING_PAYMENT, enrollment.status) },
            { assertEquals(PaymentStatus.PG_APPROVED, payment.status) },
        )
        verify(exactly = 0) { enrollmentAdaptor.save(any()) }
    }

    @Test
    fun `하나의 처리가 실패해도 나머지 enrollment는 계속 처리한다`() {
        val enrollment1 = createPendingEnrollment("pay0000000000000000001")
        val enrollment2 = createPendingEnrollment("pay0000000000000000002")
        val payment2 = createPayment("pay0000000000000000002")

        every { enrollmentAdaptor.findPendingPaymentOlderThan(any()) } returns listOf(enrollment1, enrollment2)
        every { enrollmentAdaptor.findById(enrollment1.id) } throws RuntimeException("DB 오류")
        every { enrollmentAdaptor.findById(enrollment2.id) } returns enrollment2
        every { paymentAdaptor.findById("pay0000000000000000002") } returns payment2
        every { enrollmentAdaptor.save(any()) } returns enrollment2
        every { paymentAdaptor.save(any()) } returns payment2

        job.execute()

        assertAll(
            { assertEquals(EnrollmentStatus.CANCELLED, enrollment2.status) },
            { assertEquals(PaymentStatus.CANCELLED, payment2.status) },
        )
    }
}
