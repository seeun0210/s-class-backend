package com.sclass.supporters.payment.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.coin.service.CoinDomainService
import com.sclass.domain.domains.payment.adaptor.PaymentAdaptor
import com.sclass.domain.domains.payment.domain.PaymentStatus
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CoinProduct
import com.sclass.domain.domains.product.exception.ProductTypeMismatchException
import com.sclass.infrastructure.nicepay.PgGateway
import com.sclass.infrastructure.nicepay.dto.NicePayWebhookPayload
import org.slf4j.LoggerFactory
import org.springframework.transaction.annotation.Transactional

@UseCase
class HandleNicePayWebhookUseCase(
    private val paymentAdaptor: PaymentAdaptor,
    private val productAdaptor: ProductAdaptor,
    private val coinDomainService: CoinDomainService,
    private val pgGateway: PgGateway,

) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun execute(payload: NicePayWebhookPayload) {
        if (!pgGateway.verifyWebhookSignature(
                payload.tid,
                payload.amount,
                payload.ediDate,
                payload.signature,
            )
        ) {
            log.warn("웹훅 서명 검증 실패 orderId={}", payload.orderId)
            return
        }

        val payment =
            paymentAdaptor.findByPgOrderIdOrNull(payload.orderId) ?: run {
                log.warn(
                    "웹훅 수신: 알 수 없는 pgOrderId={}",
                    payload.orderId,
                )
                return
            }

        if (payload.resultCode != RESULT_CODE_SUCCESS) {
            log.info(
                "웹훅 수신: 실패 결과 resultCode={}, orderId={}",
                payload.resultCode,
                payload.orderId,
            )
            return
        }

        when (payment.status) {
            PaymentStatus.PENDING -> {
                payment.markPgApproved(payload.tid)

                val product = productAdaptor.findById(payment.productId)
                val coinAmount = (product as? CoinProduct)?.coinAmount ?: throw ProductTypeMismatchException()

                coinDomainService.issue(
                    userId = payment.userId,
                    amount = coinAmount,
                    referenceId = payment.id,
                    description = "결제 완료 (웹훅) - ${product.name}",
                )

                payment.markCompleted()
                paymentAdaptor.save(payment)
                log.info(
                    "웹훅 수신: 결제 완료 처리 paymentId={}",
                    payment.id,
                )
            }
            PaymentStatus.COMPLETED -> {
                log.info(
                    "웹훅 수신: 이미 완료된 결제 paymentId={}",
                    payment.id,
                )
            }
            else -> {
                log.warn("웹훅 수신: 처리 불가 상태 status={}, paymentId={}", payment.status, payment.id)
            }
        }
    }

    companion object {
        private const val RESULT_CODE_SUCCESS = "0000"
    }
}
