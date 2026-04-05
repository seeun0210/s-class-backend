package com.sclass.supporters.payment.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.common.vo.Ulid
import com.sclass.domain.domains.payment.adaptor.PaymentAdaptor
import com.sclass.domain.domains.payment.domain.Payment
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.supporters.payment.dto.PreparePaymentRequest
import com.sclass.supporters.payment.dto.PreparePaymentResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class PreparePaymentUseCase(
    private val productAdaptor: ProductAdaptor,
    private val paymentAdaptor: PaymentAdaptor,
) {
    @Transactional
    fun execute(
        userId: String,
        request: PreparePaymentRequest,
    ): PreparePaymentResponse {
        val product = productAdaptor.findById(request.productId)

        val payment =
            paymentAdaptor.save(
                Payment(
                    userId = userId,
                    productId = product.id,
                    amount = product.priceWon,
                    pgType = request.pgType,
                    pgOrderId = Ulid.generate(),
                ),
            )

        return PreparePaymentResponse(
            paymentId = payment.id,
            pgOrderId = payment.pgOrderId,
            amount = payment.amount,
            productName = product.name,
        )
    }
}
