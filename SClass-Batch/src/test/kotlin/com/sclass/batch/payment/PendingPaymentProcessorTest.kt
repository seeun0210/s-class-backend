package com.sclass.batch.payment

import com.sclass.domain.domains.coin.service.CoinDomainService
import com.sclass.domain.domains.payment.adaptor.PaymentAdaptor
import com.sclass.domain.domains.payment.domain.Payment
import com.sclass.domain.domains.payment.domain.PaymentStatus
import com.sclass.domain.domains.payment.domain.PgType
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CoinProduct
import com.sclass.infrastructure.nicepay.PgGateway
import com.sclass.infrastructure.nicepay.dto.PgInquiryResult
import com.sclass.infrastructure.nicepay.exception.NicePayException
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.transaction.support.TransactionTemplate

class PendingPaymentProcessorTest {
    private lateinit var paymentAdaptor: PaymentAdaptor
    private lateinit var productAdaptor: ProductAdaptor
    private lateinit var coinDomainService: CoinDomainService
    private lateinit var pgGateway: PgGateway
    private lateinit var txTemplate: TransactionTemplate
    private lateinit var completePaymentUseCase: CompletePaymentUseCase
    private lateinit var processor: PendingPaymentProcessor

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
        completePaymentUseCase = CompletePaymentUseCase(paymentAdaptor, productAdaptor, coinDomainService, txTemplate)
        processor = PendingPaymentProcessor(pgGateway, completePaymentUseCase)
    }

    private fun createPendingPayment() =
        Payment(
            userId = "user-id-0000000000000000000001",
            productId = "product-id-000000000000000000001",
            amount = 1000,
            pgType = PgType.NICEPAY,
            pgOrderId = "order-id-000000000000000000001",
        )

    @Test
    fun `PG 승인된 결제는 코인 발급 후 COMPLETED 처리된다`() {
        val payment = createPendingPayment()
        val product = CoinProduct(name = "코인 100", priceWon = 1000, coinAmount = 100)

        every { pgGateway.inquiry(any()) } returns PgInquiryResult(approved = true, tid = "nicepay-tid-001", amount = 1000)
        every { productAdaptor.findById(any()) } returns product
        every { paymentAdaptor.findById(any()) } returns payment
        justRun { coinDomainService.issue(any(), any(), any(), any()) }

        processor.process(payment)

        assertEquals(PaymentStatus.COMPLETED, payment.status)
        verify { coinDomainService.issue(payment.userId, 100, payment.id, any()) }
    }

    @Test
    fun `PG 미승인 결제는 PG_APPROVE_FAILED 처리된다`() {
        val payment = createPendingPayment()

        every { pgGateway.inquiry(any()) } returns PgInquiryResult(approved = false, tid = null, amount = 0)
        every { paymentAdaptor.findById(any()) } returns payment

        processor.process(payment)

        assertEquals(PaymentStatus.PG_APPROVE_FAILED, payment.status)
    }

    @Test
    fun `NicePay 조회 실패 시 COMPENSATION_NEEDED 처리된다`() {
        val payment = createPendingPayment()

        every { pgGateway.inquiry(any()) } throws NicePayException("조회 실패")
        every { paymentAdaptor.save(any()) } answers { firstArg() }

        processor.process(payment)

        assertEquals(PaymentStatus.COMPENSATION_NEEDED, payment.status)
    }

    @Test
    fun `PG 승인 후 코인 발급 실패 시 ISSUE_COIN_FAILED 처리된다`() {
        val payment = createPendingPayment()
        val pgApprovedPayment = createPendingPayment()
        pgApprovedPayment.markPgApproved("nicepay-tid-001")
        val product = CoinProduct(name = "코인 100", priceWon = 1000, coinAmount = 100)

        every { pgGateway.inquiry(any()) } returns PgInquiryResult(approved = true, tid = "nicepay-tid-001", amount = 1000)
        every { productAdaptor.findById(any()) } returns product
        // findById: TX1(markPgApproved) → TX2(coin issue 실패) → TX3(markIssueCoinFailed)
        every { paymentAdaptor.findById(any()) } returns payment andThen payment andThen pgApprovedPayment
        every { coinDomainService.issue(any(), any(), any(), any()) } throws RuntimeException("코인 발급 DB 에러")

        processor.process(payment)

        assertEquals(PaymentStatus.ISSUE_COIN_FAILED, pgApprovedPayment.status)
    }

    @Test
    fun `이미 처리된 결제는 무시한다`() {
        val payment = createPendingPayment()
        val completedPayment = createPendingPayment()
        completedPayment.markPgApproved("tid-001")
        completedPayment.markCompleted()

        every { pgGateway.inquiry(any()) } returns PgInquiryResult(approved = true, tid = "nicepay-tid-001", amount = 1000)
        every { paymentAdaptor.findById(any()) } returns completedPayment

        processor.process(payment)

        verify(exactly = 0) { coinDomainService.issue(any(), any(), any(), any()) }
    }
}
