package com.sclass.domain.domains.payment.adaptor

import com.sclass.domain.domains.payment.domain.Payment
import com.sclass.domain.domains.payment.domain.PgType
import com.sclass.domain.domains.payment.exception.PaymentNotFoundException
import com.sclass.domain.domains.payment.repository.PaymentRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Optional

class PaymentAdaptorTest {
    private lateinit var paymentRepository: PaymentRepository
    private lateinit var paymentAdaptor: PaymentAdaptor

    @BeforeEach
    fun setUp() {
        paymentRepository = mockk()
        paymentAdaptor = PaymentAdaptor(paymentRepository)
    }

    private fun createPayment() =
        Payment(
            userId = "user-id-00000000000",
            productId = "product-id-000000",
            amount = 10000,
            pgType = PgType.NICEPAY,
            pgOrderId = "order-001",
        )

    @Nested
    inner class FindById {
        @Test
        fun `존재하는 id로 조회하면 결제를 반환한다`() {
            val payment = createPayment()
            every { paymentRepository.findById(payment.id) } returns Optional.of(payment)

            val result = paymentAdaptor.findById(payment.id)

            assertEquals(payment, result)
        }

        @Test
        fun `존재하지 않는 id로 조회하면 PaymentNotFoundException이 발생한다`() {
            every { paymentRepository.findById(any()) } returns Optional.empty()

            assertThrows<PaymentNotFoundException> {
                paymentAdaptor.findById("non-existent-id-0000")
            }
        }
    }

    @Nested
    inner class FindByPgOrderIdOrNull {
        @Test
        fun `존재하지 않는 pgOrderId로 조회하면 null을 반환한다`() {
            every { paymentRepository.findByPgOrderId("unknown") } returns null

            val result = paymentAdaptor.findByPgOrderIdOrNull("unknown")

            assertNull(result)
        }
    }

    @Nested
    inner class Save {
        @Test
        fun `결제 저장을 repository에 위임한다`() {
            val payment = createPayment()
            every { paymentRepository.save(payment) } returns payment

            val result = paymentAdaptor.save(payment)

            assertEquals(payment, result)
            verify { paymentRepository.save(payment) }
        }
    }
}
