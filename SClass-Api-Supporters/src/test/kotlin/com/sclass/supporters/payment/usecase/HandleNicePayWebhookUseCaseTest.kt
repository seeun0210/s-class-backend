package com.sclass.supporters.payment.usecase

import com.sclass.domain.domains.coin.service.CoinDomainService
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.enrollment.domain.Enrollment
import com.sclass.domain.domains.enrollment.domain.EnrollmentStatus
import com.sclass.domain.domains.payment.adaptor.PaymentAdaptor
import com.sclass.domain.domains.payment.domain.Payment
import com.sclass.domain.domains.payment.domain.PaymentStatus
import com.sclass.domain.domains.payment.domain.PgType
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CoinProduct
import com.sclass.domain.domains.product.domain.CourseProduct
import com.sclass.infrastructure.nicepay.PgGateway
import com.sclass.infrastructure.nicepay.dto.NicePayWebhookPayload
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.transaction.support.TransactionTemplate

class HandleNicePayWebhookUseCaseTest {
    private lateinit var paymentAdaptor: PaymentAdaptor
    private lateinit var productAdaptor: ProductAdaptor
    private lateinit var coinDomainService: CoinDomainService
    private lateinit var enrollmentAdaptor: EnrollmentAdaptor
    private lateinit var pgGateway: PgGateway
    private lateinit var txTemplate: TransactionTemplate
    private lateinit var useCase: HandleNicePayWebhookUseCase

    @BeforeEach
    fun setUp() {
        paymentAdaptor = mockk()
        productAdaptor = mockk()
        coinDomainService = mockk()
        pgGateway = mockk()
        txTemplate = mockk()
        every { txTemplate.execute(any<org.springframework.transaction.support.TransactionCallback<Any?>>()) } answers {
            val callback = firstArg<org.springframework.transaction.support.TransactionCallback<Any?>>()
            callback.doInTransaction(mockk())
        }
        every { paymentAdaptor.save(any()) } answers { firstArg() }
        enrollmentAdaptor = mockk()
        useCase = HandleNicePayWebhookUseCase(paymentAdaptor, productAdaptor, coinDomainService, pgGateway, txTemplate, enrollmentAdaptor)
    }

    @Test
    fun `정상 웹훅 수신 시 PG_APPROVED 후 코인 발급하고 COMPLETED 처리된다`() {
        val payment = pendingPayment()
        val product = CoinProduct(name = "코인 100", priceWon = 1000, coinAmount = 100)

        every { pgGateway.verifyWebhookSignature(any(), any(), any(), any()) } returns true
        every { paymentAdaptor.findByPgOrderIdOrNull(any()) } returns payment
        every { paymentAdaptor.findById(payment.id) } returns payment
        every { productAdaptor.findById(payment.productId) } returns product
        justRun { coinDomainService.issue(any(), any(), any(), any()) }

        useCase.execute(payment.pgOrderId, successPayload(payment.pgOrderId))

        assertAll(
            { assertEquals(PaymentStatus.COMPLETED, payment.status) },
        )
        verify { coinDomainService.issue(payment.userId, 100, payment.id, any()) }
    }

    @Test
    fun `코인 발급 실패 시 ISSUE_COIN_FAILED로 마킹된다`() {
        val payment = pendingPayment()
        val pgApprovedPayment = pendingPayment()
        pgApprovedPayment.markPgApproved("tid-001")
        val product = CoinProduct(name = "코인 100", priceWon = 1000, coinAmount = 100)

        every { pgGateway.verifyWebhookSignature(any(), any(), any(), any()) } returns true
        every { paymentAdaptor.findByPgOrderIdOrNull(any()) } returns payment
        every { productAdaptor.findById(payment.productId) } returns product
        // findById: TX1(markPgApproved) → TX2(coin issue 실패) → TX3(markIssueCoinFailed)
        every { paymentAdaptor.findById(payment.id) } returns payment andThen payment andThen pgApprovedPayment
        every { coinDomainService.issue(any(), any(), any(), any()) } throws RuntimeException("코인 발급 실패")

        useCase.execute(payment.pgOrderId, successPayload(payment.pgOrderId))

        assertEquals(PaymentStatus.ISSUE_COIN_FAILED, pgApprovedPayment.status)
    }

    @Test
    fun `서명 검증 실패 시 처리하지 않는다`() {
        every { pgGateway.verifyWebhookSignature(any(), any(), any(), any()) } returns false

        useCase.execute("order-001", successPayload("order-001"))

        verify(exactly = 0) { paymentAdaptor.findByPgOrderIdOrNull(any()) }
    }

    @Test
    fun `resultCode가 0000이 아니면 처리하지 않는다`() {
        val payment = pendingPayment()

        every { pgGateway.verifyWebhookSignature(any(), any(), any(), any()) } returns true
        every { paymentAdaptor.findByPgOrderIdOrNull(any()) } returns payment

        val failurePayload = webhookPayload(payment.pgOrderId, resultCode = "F100")
        useCase.execute(payment.pgOrderId, failurePayload)

        assertEquals(PaymentStatus.PENDING, payment.status)
    }

    @Test
    fun `이미 COMPLETED 상태인 결제는 무시한다`() {
        val payment = pendingPayment()
        payment.markPgApproved("tid-001")
        payment.markCompleted()

        every { pgGateway.verifyWebhookSignature(any(), any(), any(), any()) } returns true
        every { paymentAdaptor.findByPgOrderIdOrNull(any()) } returns payment

        useCase.execute(payment.pgOrderId, successPayload(payment.pgOrderId))

        verify(exactly = 0) { coinDomainService.issue(any(), any(), any(), any()) }
    }

    @Test
    fun `존재하지 않는 orderId면 무시한다`() {
        every { pgGateway.verifyWebhookSignature(any(), any(), any(), any()) } returns true
        every { paymentAdaptor.findByPgOrderIdOrNull(any()) } returns null

        useCase.execute("unknown-order", successPayload("unknown-order"))

        verify(exactly = 0) { productAdaptor.findById(any()) }
    }

    @Test
    fun `CourseProduct 결제 시 enrollment가 없으면 무시한다`() {
        val payment = pendingPayment()
        val product = CourseProduct(name = "수학 코스", priceWon = 300000, totalLessons = 12, teacherPayoutPerLessonWon = 20000)

        every { pgGateway.verifyWebhookSignature(any(), any(), any(), any()) } returns true
        every { paymentAdaptor.findByPgOrderIdOrNull(any()) } returns payment
        every { paymentAdaptor.findById(payment.id) } returns payment
        every { paymentAdaptor.save(any()) } answers { firstArg() }
        every { productAdaptor.findById(any()) } returns product
        every { enrollmentAdaptor.findByPaymentIdOrNull(payment.id) } returns null

        useCase.execute(payment.pgOrderId, successPayload(payment.pgOrderId))

        verify(exactly = 0) { enrollmentAdaptor.save(any()) }
    }

    @Test
    fun `CourseProduct 결제 시 enrollment가 있으면 ACTIVE 처리된다`() {
        val payment = pendingPayment()
        val product = CourseProduct(name = "수학 코스", priceWon = 300000, totalLessons = 12, teacherPayoutPerLessonWon = 20000)
        val enrollment =
            Enrollment.createForPurchase(
                courseId = 1L,
                studentUserId = "student-id-000000000001",
                tuitionAmountWon = 300000,
                teacherPayoutPerLessonWon = 20000,
                paymentId = payment.id,
            )

        every { pgGateway.verifyWebhookSignature(any(), any(), any(), any()) } returns true
        every { paymentAdaptor.findByPgOrderIdOrNull(any()) } returns payment
        every { paymentAdaptor.findById(payment.id) } returns payment
        every { paymentAdaptor.save(any()) } answers { firstArg() }
        every { productAdaptor.findById(any()) } returns product
        every { enrollmentAdaptor.findByPaymentIdOrNull(payment.id) } returns enrollment
        every { enrollmentAdaptor.findByPaymentId(payment.id) } returns enrollment
        every { enrollmentAdaptor.save(any()) } answers { firstArg() }

        useCase.execute(payment.pgOrderId, successPayload(payment.pgOrderId))

        assertEquals(EnrollmentStatus.ACTIVE, enrollment.status)
    }

    private fun pendingPayment() =
        Payment(
            userId = "user-00000000000000000000000001",
            productId = "prod-00000000000000000000000001",
            amount = 1000,
            pgType = PgType.NICEPAY,
            pgOrderId = "order-00000000000000000000000001",
        )

    private fun successPayload(orderId: String) = webhookPayload(orderId, resultCode = "0000")

    private fun webhookPayload(
        orderId: String,
        resultCode: String,
    ) = NicePayWebhookPayload(
        tid = "tid-001",
        orderId = orderId,
        amount = 1000,
        ediDate = "20260407120000",
        signature = "valid-sig",
        resultCode = resultCode,
        resultMsg = "성공",
    )
}
