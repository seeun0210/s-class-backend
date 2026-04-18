package com.sclass.supporters.payment.usecase

import com.sclass.domain.domains.coin.adaptor.CoinPackageAdaptor
import com.sclass.domain.domains.coin.domain.CoinPackage
import com.sclass.domain.domains.coin.service.CoinDomainService
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.payment.adaptor.PaymentAdaptor
import com.sclass.domain.domains.payment.domain.Payment
import com.sclass.domain.domains.payment.domain.PaymentStatus
import com.sclass.domain.domains.payment.domain.PaymentTargetType
import com.sclass.domain.domains.payment.domain.PgType
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
import org.springframework.transaction.support.TransactionTemplate

class HandleNicePayReturnUseCaseTest {
    private lateinit var paymentAdaptor: PaymentAdaptor
    private lateinit var coinPackageAdaptor: CoinPackageAdaptor
    private lateinit var coinDomainService: CoinDomainService
    private lateinit var enrollmentAdaptor: EnrollmentAdaptor
    private lateinit var pgGateway: PgGateway
    private lateinit var txTemplate: TransactionTemplate
    private lateinit var useCase: HandleNicePayReturnUseCase

    private val frontendUrl = "https://sclass.aura.co.kr"

    @BeforeEach
    fun setUp() {
        paymentAdaptor = mockk()
        coinPackageAdaptor = mockk()
        coinDomainService = mockk()
        pgGateway = mockk()
        txTemplate = mockk()
        every { txTemplate.execute(any<org.springframework.transaction.support.TransactionCallback<Any?>>()) } answers {
            val callback = firstArg<org.springframework.transaction.support.TransactionCallback<Any?>>()
            callback.doInTransaction(mockk())
        }
        every { paymentAdaptor.save(any()) } answers { firstArg() }
        enrollmentAdaptor = mockk()
        useCase =
            HandleNicePayReturnUseCase(
                paymentAdaptor,
                coinPackageAdaptor,
                coinDomainService,
                enrollmentAdaptor,
                pgGateway,
                txTemplate,
                frontendUrl,
            )
    }

    @Test
    fun `결제 성공 시 issuedCoinAmount와 함께 COMPLETED 콜백 URL을 반환한다`() {
        val payment = pendingPayment(amount = 1000)
        val coinPackage = CoinPackage(name = "코인 100", priceWon = 1000, coinAmount = 100)

        every { pgGateway.verifyReturnSignature(any(), any(), any()) } returns true
        every { paymentAdaptor.findByPgOrderIdOrNull(payment.pgOrderId) } returns payment
        every { paymentAdaptor.findById(payment.id) } returns payment
        every { coinPackageAdaptor.findById(payment.targetId) } returns coinPackage
        every { pgGateway.approve(any(), any(), any()) } returns
            PgApproveResult(tid = "tid-001", pgOrderId = payment.pgOrderId, amount = 1000)
        justRun { coinDomainService.issue(any(), any(), any(), any()) }

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
            { assertEquals(PaymentStatus.COMPLETED, payment.status) },
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
    fun `PG 승인 실패 시 PG_APPROVE_FAILED 상태로 변경하고 FAILED URL을 반환한다`() {
        val payment = pendingPayment(amount = 1000)

        every { pgGateway.verifyReturnSignature(any(), any(), any()) } returns true
        every { paymentAdaptor.findByPgOrderIdOrNull(payment.pgOrderId) } returns payment
        every { paymentAdaptor.findById(payment.id) } returns payment
        every { pgGateway.approve(any(), any(), any()) } throws NicePayException("승인 실패")

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
    }

    @Test
    fun `코인 발급 실패 시 PG_APPROVED는 유지되고 ISSUE_COIN_FAILED로 마킹된다`() {
        val payment = pendingPayment(amount = 1000)
        val pgApprovedPayment = pendingPayment(amount = 1000)
        pgApprovedPayment.markPgApproved("tid-001")
        val coinPackage = CoinPackage(name = "코인 100", priceWon = 1000, coinAmount = 100)

        every { pgGateway.verifyReturnSignature(any(), any(), any()) } returns true
        every { paymentAdaptor.findByPgOrderIdOrNull(payment.pgOrderId) } returns payment
        every { coinPackageAdaptor.findById(payment.targetId) } returns coinPackage
        every { pgGateway.approve(any(), any(), any()) } returns
            PgApproveResult(tid = "tid-001", pgOrderId = payment.pgOrderId, amount = 1000)

        // findById 호출 순서: TX1(markPgApproved) → TX2(coin issue) → TX3(markIssueCoinFailed)
        every { paymentAdaptor.findById(payment.id) } returns payment andThen payment andThen pgApprovedPayment
        every { coinDomainService.issue(any(), any(), any(), any()) } throws RuntimeException("코인 발급 DB 에러")

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
            { assertTrue(result.contains("status=FAILED"), "실패 URL이 반환되어야 한다") },
            { assertEquals(PaymentStatus.ISSUE_COIN_FAILED, pgApprovedPayment.status, "코인 발급 실패 상태로 마킹되어야 한다") },
        )
        verify(atLeast = 1) { paymentAdaptor.save(any()) }
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
            targetType = PaymentTargetType.COIN_PACKAGE,
            targetId = "cp-000000000000000000000000001",
            amount = amount,
            pgType = PgType.NICEPAY,
            pgOrderId = "order-00000000000000000000000001",
        )
}
