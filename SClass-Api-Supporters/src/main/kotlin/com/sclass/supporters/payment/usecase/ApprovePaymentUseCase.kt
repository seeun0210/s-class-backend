package com.sclass.supporters.payment.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.coin.service.CoinDomainService
import com.sclass.domain.domains.payment.adaptor.PaymentAdaptor
import com.sclass.domain.domains.payment.domain.PaymentStatus
import com.sclass.domain.domains.payment.exception.InvalidPaymentStatusException
import com.sclass.domain.domains.payment.exception.PaymentPgApproveFailedException
import com.sclass.domain.domains.payment.exception.PaymentUnauthorizedException
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CoinProduct
import com.sclass.domain.domains.product.exception.ProductTypeMismatchException
import com.sclass.infrastructure.nicepay.PgGateway
import com.sclass.infrastructure.nicepay.exception.NicePayException
import com.sclass.supporters.payment.dto.ApprovePaymentResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class ApprovePaymentUseCase(
    private val paymentAdaptor: PaymentAdaptor,
    private val productAdaptor: ProductAdaptor,
    private val coinDomainService: CoinDomainService,
    private val pgGateway: PgGateway,
) {
    @Transactional
    fun execute(
        userId: String,
        paymentId: String,
        tid: String,
    ): ApprovePaymentResponse {
        val payment = paymentAdaptor.findById(paymentId)

        if (payment.userId != userId) throw PaymentUnauthorizedException()
        if (payment.status != PaymentStatus.PENDING) throw InvalidPaymentStatusException()

        val pgResult =
            try {
                pgGateway.approve(payment.pgOrderId, tid, payment.amount)
            } catch (e: NicePayException) {
                throw PaymentPgApproveFailedException()
            }

        payment.markPgApproved(pgResult.tid)

        val product = productAdaptor.findById(payment.productId)
        val coinAmount = (product as? CoinProduct)?.coinAmount ?: throw ProductTypeMismatchException()

        coinDomainService.issue(
            userId = userId,
            amount = coinAmount,
            referenceId = payment.id,
            description = "결제 완료 - ${product.name}",
        )

        payment.markCompleted()
        paymentAdaptor.save(payment)

        return ApprovePaymentResponse(
            paymentId = payment.id,
            status = payment.status,
            issuedCoinAmount = coinAmount,
        )
    }
}
