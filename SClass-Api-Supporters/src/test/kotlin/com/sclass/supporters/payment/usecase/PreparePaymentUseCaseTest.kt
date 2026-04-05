package com.sclass.supporters.payment.usecase

import com.sclass.domain.domains.payment.adaptor.PaymentAdaptor
import com.sclass.domain.domains.payment.domain.Payment
import com.sclass.domain.domains.payment.domain.PgType
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CoinProduct
import com.sclass.domain.domains.product.exception.ProductNotFoundException
import com.sclass.supporters.payment.dto.PreparePaymentRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PreparePaymentUseCaseTest {
    private lateinit var productAdaptor: ProductAdaptor
    private lateinit var paymentAdaptor: PaymentAdaptor
    private lateinit var useCase: PreparePaymentUseCase

    @BeforeEach
    fun setUp() {
        productAdaptor = mockk()
        paymentAdaptor = mockk()
        useCase = PreparePaymentUseCase(productAdaptor, paymentAdaptor)
    }

    @Test
    fun `결제 준비 성공 시 paymentId와 pgOrderId를 반환한다`() {
        val product = CoinProduct(name = "코인 100", priceWon = 1000, coinAmount = 100)
        every { productAdaptor.findById(any()) } returns product

        val paymentSlot = slot<Payment>()
        every { paymentAdaptor.save(capture(paymentSlot)) } answers { paymentSlot.captured }

        val result =
            useCase.execute(
                userId = "user-id-00000000000000000000000001",
                request = PreparePaymentRequest(productId = product.id, pgType = PgType.NICEPAY),
            )

        assertAll(
            { assertEquals(product.priceWon, result.amount) },
            { assertEquals(product.name, result.productName) },
            { assertEquals(paymentSlot.captured.pgOrderId, result.pgOrderId) },
        )
    }

    @Test
    fun `존재하지 않는 상품이면 예외가 발생한다`() {
        every { productAdaptor.findById(any()) } throws ProductNotFoundException()

        assertThrows<ProductNotFoundException> {
            useCase.execute(
                userId = "user-id-00000000000000000000000001",
                request = PreparePaymentRequest(productId = "invalid-id", pgType = PgType.NICEPAY),
            )
        }
    }
}
