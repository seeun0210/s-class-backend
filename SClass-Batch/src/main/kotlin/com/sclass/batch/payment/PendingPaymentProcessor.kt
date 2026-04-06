package com.sclass.batch.payment

import com.sclass.domain.domains.payment.domain.Payment
import com.sclass.infrastructure.nicepay.PgGateway
import com.sclass.infrastructure.nicepay.exception.NicePayException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class PendingPaymentProcessor(
    private val pgGateway: PgGateway,
    private val completePaymentUseCase: CompletePaymentUseCase,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun process(payment: Payment) {
        val result =
            try {
                pgGateway.inquiry(payment.pgOrderId)
            } catch (e: NicePayException) {
                completePaymentUseCase.handlePgInquiryFailed(payment, e)
                return
            }

        completePaymentUseCase.execute(payment.pgOrderId, payment, result)
    }
}
