package com.sclass.supporters.payment.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.common.vo.Ulid
import com.sclass.domain.domains.coin.adaptor.CoinPackageAdaptor
import com.sclass.domain.domains.payment.adaptor.PaymentAdaptor
import com.sclass.domain.domains.payment.domain.Payment
import com.sclass.domain.domains.payment.domain.PaymentTargetType
import com.sclass.supporters.payment.dto.PreparePaymentRequest
import com.sclass.supporters.payment.dto.PreparePaymentResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class PreparePaymentUseCase(
    private val coinPackageAdaptor: CoinPackageAdaptor,
    private val paymentAdaptor: PaymentAdaptor,
) {
    @Transactional
    fun execute(
        userId: String,
        request: PreparePaymentRequest,
    ): PreparePaymentResponse {
        val coinPackage = coinPackageAdaptor.findById(request.coinPackageId)

        val payment =
            paymentAdaptor.save(
                Payment(
                    userId = userId,
                    targetType = PaymentTargetType.COIN_PACKAGE,
                    targetId = coinPackage.id,
                    amount = coinPackage.priceWon,
                    pgType = request.pgType,
                    pgOrderId = Ulid.generate(),
                ),
            )

        return PreparePaymentResponse(
            paymentId = payment.id,
            pgOrderId = payment.pgOrderId,
            amount = payment.amount,
            productName = coinPackage.name,
        )
    }
}
