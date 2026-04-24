package com.sclass.supporters.payment.usecase

import com.sclass.domain.domains.payment.adaptor.PaymentAdaptor
import com.sclass.domain.domains.payment.domain.Payment
import com.sclass.domain.domains.payment.domain.PaymentTargetType
import com.sclass.domain.domains.payment.domain.PgType
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AbandonPaymentUseCaseFacadeTest {
    private lateinit var paymentAdaptor: PaymentAdaptor
    private lateinit var abandonPaymentLockedUseCase: AbandonPaymentLockedUseCase
    private lateinit var useCase: AbandonPaymentUseCase

    @BeforeEach
    fun setUp() {
        paymentAdaptor = mockk()
        abandonPaymentLockedUseCase = mockk()
        useCase = AbandonPaymentUseCase(paymentAdaptor, abandonPaymentLockedUseCase)
    }

    @Test
    fun `paymentId로 pgOrderId를 해석한 뒤 locked use case에 위임한다`() {
        val payment =
            Payment(
                userId = "user-id-1",
                targetType = PaymentTargetType.COURSE_PRODUCT,
                targetId = "product-id-1",
                amount = 300000,
                pgType = PgType.NICEPAY,
                pgOrderId = "order-id-1",
            )

        every { paymentAdaptor.findById(payment.id) } returns payment
        every {
            abandonPaymentLockedUseCase.execute(
                userId = payment.userId,
                paymentId = payment.id,
                pgOrderId = payment.pgOrderId,
            )
        } returns Unit

        useCase.execute(payment.userId, payment.id)

        verify(exactly = 1) { paymentAdaptor.findById(payment.id) }
        verify(exactly = 1) {
            abandonPaymentLockedUseCase.execute(
                userId = payment.userId,
                paymentId = payment.id,
                pgOrderId = payment.pgOrderId,
            )
        }
    }
}
