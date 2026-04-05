package com.sclass.supporters.payment.usecase

import com.sclass.domain.domains.coin.service.CoinDomainService
import com.sclass.domain.domains.payment.adaptor.PaymentAdaptor
import com.sclass.domain.domains.payment.domain.Payment
import com.sclass.domain.domains.payment.domain.PaymentStatus
import com.sclass.domain.domains.payment.domain.PgType
import com.sclass.domain.domains.payment.exception.PaymentPgApproveFailedException
import com.sclass.domain.domains.payment.exception.PaymentUnauthorizedException
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ApprovePaymentUseCaseTest {
    private lateinit var paymentAdaptor: PaymentAdaptor
    private lateinit var productAdaptor: ProductAdaptor
    private lateinit var coinDomainService: CoinDomainService
    private lateinit var pgGateway: PgGateway
    private lateinit var useCase: ApprovePaymentUseCase

    @BeforeEach
    fun setUp() {
        paymentAdaptor = mockk()
        productAdaptor = mockk()
        coinDomainService = mockk()
        pgGateway = mockk()
        useCase = ApprovePaymentUseCase(paymentAdaptor, productAdaptor, coinDomainService, pgGateway)
    }

    private fun createPendingPayment(userId: String = "user-id-0000000000000000000001") =
        Payment(
            userId = userId,
            productId = "product-id-000000000000000000001",
            amount = 1000,
            pgType = PgType.NICEPAY,
            pgOrderId = "order-id-000000000000000000001",
        )

    @Test
    fun `결제 승인 성공 시 COMPLETED 상태와 발급 코인량을 반환한다`() {
        val userId = "user-id-0000000000000000000001"
        val payment = createPendingPayment(userId)
        val product = CoinProduct(name = "코인 100", priceWon = 1000, coinAmount = 100)

        every { paymentAdaptor.findById(any()) } returns payment
        every { pgGateway.approve(any(), any(), any()) } returns
            PgApproveResult(
                tid = "nicepay-tid-001",
                pgOrderId = payment.pgOrderId,
                amount = payment.amount,
            )
        every { productAdaptor.findById(any()) } returns product
        justRun { coinDomainService.issue(any(), any(), any(), any()) }
        every { paymentAdaptor.save(any()) } answers { firstArg() }

        val result = useCase.execute(userId, payment.id, "nicepay-tid-001")

        assertAll(
            { assertEquals(PaymentStatus.COMPLETED, result.status) },
            { assertEquals(100, result.issuedCoinAmount) },
        )
        verify { coinDomainService.issue(userId, 100, payment.id, any()) }
    }

    @Test
    fun `다른 사용자의 결제를 승인하면 예외가 발생한다`() {
        val payment = createPendingPayment(userId = "other-user-id-00000000000000001")
        every { paymentAdaptor.findById(any()) } returns payment

        assertThrows<PaymentUnauthorizedException> {
            useCase.execute("user-id-0000000000000000000001", payment.id, "tid")
        }
    }

    @Test
    fun `PG 승인 실패 시 예외가 발생한다`() {
        val userId = "user-id-0000000000000000000001"
        val payment = createPendingPayment(userId)

        every { paymentAdaptor.findById(any()) } returns payment
        every { pgGateway.approve(any(), any(), any()) } throws NicePayException("PG 오류")

        assertThrows<PaymentPgApproveFailedException> {
            useCase.execute(userId, payment.id, "invalid-tid")
        }
    }
}
