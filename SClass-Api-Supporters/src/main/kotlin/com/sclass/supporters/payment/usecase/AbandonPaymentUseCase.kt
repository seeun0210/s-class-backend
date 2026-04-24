package com.sclass.supporters.payment.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.payment.adaptor.PaymentAdaptor

@UseCase
class AbandonPaymentUseCase(
    private val paymentAdaptor: PaymentAdaptor,
    private val abandonPaymentLockedUseCase: AbandonPaymentLockedUseCase,
) {
    fun execute(
        userId: String,
        paymentId: String,
    ) = paymentAdaptor.findById(paymentId).let { payment ->
        abandonPaymentLockedUseCase.execute(
            userId = userId,
            paymentId = payment.id,
            pgOrderId = payment.pgOrderId,
        )
    }
}
