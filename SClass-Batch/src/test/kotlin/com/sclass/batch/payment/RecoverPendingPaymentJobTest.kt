package com.sclass.batch.payment

import com.sclass.domain.domains.payment.adaptor.PaymentAdaptor
import com.sclass.domain.domains.payment.domain.Payment
import com.sclass.domain.domains.payment.domain.PaymentTargetType
import com.sclass.domain.domains.payment.domain.PgType
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RecoverPendingPaymentJobTest {
    private lateinit var paymentAdaptor: PaymentAdaptor
    private lateinit var processor: PendingPaymentProcessor
    private lateinit var job: RecoverPendingPaymentJob

    @BeforeEach
    fun setUp() {
        paymentAdaptor = mockk()
        processor = mockk()
        job = RecoverPendingPaymentJob(paymentAdaptor, processor)
    }

    private fun createPendingPayment(id: String = "payment-id-00000000000000000001") =
        Payment(
            userId = "user-id-0000000000000000000001",
            targetType = PaymentTargetType.COIN_PACKAGE,
            targetId = "coin-pkg-id-00000000000000001",
            amount = 1000,
            pgType = PgType.NICEPAY,
            pgOrderId = "order-id-$id",
        )

    @Test
    fun `PENDING 결제가 없으면 아무것도 처리하지 않는다`() {
        every { paymentAdaptor.findPendingOlderThan(any()) } returns emptyList()

        job.execute()

        verify(exactly = 0) { processor.process(any()) }
    }

    @Test
    fun `PENDING 결제가 50건 이하면 건별로 processor를 호출한다`() {
        val payments = (1..3).map { createPendingPayment("id-$it") }
        every { paymentAdaptor.findPendingOlderThan(any()) } returns payments
        justRun { processor.process(any()) }

        job.execute()

        verify(exactly = 3) { processor.process(any()) }
    }

    @Test
    fun `PENDING 결제가 50건 초과면 최대 50건만 처리하고 나머지는 다음 주기로 넘긴다`() {
        val payments = (1..51).map { createPendingPayment("id-$it") }
        every { paymentAdaptor.findPendingOlderThan(any()) } returns payments
        justRun { processor.process(any()) }

        job.execute()

        verify(exactly = 50) { processor.process(any()) }
    }
}
