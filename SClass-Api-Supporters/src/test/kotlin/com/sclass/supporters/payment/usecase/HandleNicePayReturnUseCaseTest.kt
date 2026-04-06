package com.sclass.supporters.payment.usecase

import com.sclass.domain.domains.coin.service.CoinDomainService
import com.sclass.domain.domains.payment.adaptor.PaymentAdaptor
import com.sclass.domain.domains.payment.domain.Payment
import com.sclass.domain.domains.payment.domain.PaymentStatus
import com.sclass.domain.domains.payment.domain.PgType
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CoinProduct
import com.sclass.infrastructure.nicepay.PgGateway
import com.sclass.infrastructure.nicepay.dto.PgApproveResult
import com.sclass.infrastructure.nicepay.exception.NicePayException
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class HandleNicePayReturnUseCaseTest {
    private lateinit var paymentAdaptor: PaymentAdaptor
    private lateinit var productAdaptor: ProductAdaptor
    private lateinit var coinDomainService: CoinDomainService
    private lateinit var pgGateway: PgGateway
    private lateinit var useCase: HandleNicePayReturnUseCase

    private val frontendUrl = "https://sclass.aura.co.kr"

    @BeforeEach
    fun setUp() {
        paymentAdaptor = mockk()
        productAdaptor = mockk()
        coinDomainService = mockk()
        pgGateway = mockk()
        useCase = HandleNicePayReturnUseCase(paymentAdaptor, productAdaptor, coinDomainService, pgGateway, frontendUrl)
    }

    @Test
    fun `결제 성공 시 issuedCoinAmount와 함께 COMPLETED 콜백 URL을 반환한다`() {
        val payment = pendingPayment(amount = 1000)
        val product = CoinProduct(name = "코인 100", priceWon = 1000, coinAmount = 100)

        every { pgGateway.verifyReturnSignature(any(), any(), any()) } returns true
        every { paymentAdaptor.findByPgOrderIdOrNull(payment.pgOrderId) } returns payment
        every { productAdaptor.findById(payment.productId) } returns product
        every { pgGateway.approve(any(), any(), any()) } returns
            PgApproveResult(tid = "tid-001", pgOrderId = payment.pgOrderId, amount = 1000)
        justRun { coinDomainService.issue(any(), any(), any(), any()) }
        every { paymentAdaptor.save(any()) } answers { firstArg() }

        val result =
            useCase.execute(
                authResultCode = "0000",
                tid = "tid-001",
                orderId = payment.pgOrderId,
                amount = 1000,
                authToken = "token",
                signature = "sig",
            )

        assertAll(
            { assertTrue(result.contains("status=COMPLETED")) },
            { assertTrue(result.contains("issuedCoinAmount=100")) },
        )
    }

    @Test
    fun `authResultCode가 0000이 아니면 FAILED 콜백 URL을 반환한다`() {
        val result =
            useCase.execute(
                authResultCode = "F100",
                tid = "tid-001",
                orderId = "order-001",
                amount = 1000,
                authToken = "token",
                signature = "sig",
            )

        assertTrue(result.contains("status=FAILED"))
    }

    @Test
    fun `서명 검증 실패 시 FAILED 콜백 URL을 반환한다`() {
        every { pgGateway.verifyReturnSignature(any(), any(), any()) } returns false

        val result =
            useCase.execute(
                authResultCode = "0000",
                tid = "tid-001",
                orderId = "order-001",
                amount = 1000,
                authToken = "token",
                signature = "wrong-sig",
            )

        assertTrue(result.contains("status=FAILED"))
    }

    @Test
    fun `결제 금액이 불일치하면 FAILED 콜백 URL을 반환한다`() {
        val payment = pendingPayment(amount = 2000)

        every { pgGateway.verifyReturnSignature(any(), any(), any()) } returns true
        every { paymentAdaptor.findByPgOrderIdOrNull(payment.pgOrderId) } returns payment

        val result =
            useCase.execute(
                authResultCode = "0000",
                tid = "tid-001",
                orderId = payment.pgOrderId,
                amount = 1000,
                authToken = "token",
                signature = "sig",
            )

        assertTrue(result.contains("status=FAILED"))
    }

    @Test
    fun `PG 승인 실패 시 FAILED 콜백 URL을 반환하고 결제 상태를 PG_APPROVE_FAILED로 변경한다`() {
        val payment = pendingPayment(amount = 1000)
        val product = CoinProduct(name = "코인 100", priceWon = 1000, coinAmount = 100)

        every { pgGateway.verifyReturnSignature(any(), any(), any()) } returns true
        every { paymentAdaptor.findByPgOrderIdOrNull(payment.pgOrderId) } returns payment
        every { productAdaptor.findById(payment.productId) } returns product
        every { pgGateway.approve(any(), any(), any()) } throws NicePayException("승인 실패")
        every { paymentAdaptor.save(any()) } answers { firstArg() }

        val result =
            useCase.execute(
                authResultCode = "0000",
                tid = "tid-001",
                orderId = payment.pgOrderId,
                amount = 1000,
                authToken = "token",
                signature = "sig",
            )

        assertAll(
            { assertTrue(result.contains("status=FAILED")) },
            { assertEquals(PaymentStatus.PG_APPROVE_FAILED, payment.status) },
        )
        verify(exactly = 1) { paymentAdaptor.save(payment) }
    }

    @Test
    fun `이미 COMPLETED 상태인 결제는 issuedCoinAmount 없이 COMPLETED 콜백 URL을 반환한다`() {
        val payment = pendingPayment(amount = 1000)
        payment.markPgApproved("tid-001")
        payment.markCompleted()

        every { pgGateway.verifyReturnSignature(any(), any(), any()) } returns true
        every { paymentAdaptor.findByPgOrderIdOrNull(payment.pgOrderId) } returns payment

        val result =
            useCase.execute(
                authResultCode = "0000",
                tid = "tid-001",
                orderId = payment.pgOrderId,
                amount = 1000,
                authToken = "token",
                signature = "sig",
            )

        assertAll(
            { assertTrue(result.contains("status=COMPLETED")) },
            { assertTrue(!result.contains("issuedCoinAmount")) },
        )
    }

    @Test
    fun `필수 파라미터가 null이면 FAILED 콜백 URL을 반환한다`() {
        val result =
            useCase.execute(
                authResultCode = "0000",
                tid = null,
                orderId = "order-001",
                amount = 1000,
                authToken = null,
                signature = null,
            )

        assertTrue(result.contains("status=FAILED"))
    }

    private fun pendingPayment(amount: Int = 1000) =
        Payment(
            userId = "user-00000000000000000000000001",
            productId = "prod-00000000000000000000000001",
            amount = amount,
            pgType = PgType.NICEPAY,
            pgOrderId = "order-00000000000000000000000001",
        )
}
