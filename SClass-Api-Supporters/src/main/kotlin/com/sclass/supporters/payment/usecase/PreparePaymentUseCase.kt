package com.sclass.supporters.payment.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.common.vo.Ulid
import com.sclass.domain.domains.coin.adaptor.CoinPackageAdaptor
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.enrollment.exception.NoActiveMembershipException
import com.sclass.domain.domains.payment.adaptor.PaymentAdaptor
import com.sclass.domain.domains.payment.domain.Payment
import com.sclass.domain.domains.payment.domain.PaymentTargetType
import com.sclass.supporters.payment.dto.PreparePaymentRequest
import com.sclass.supporters.payment.dto.PreparePaymentResponse
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDateTime

@UseCase
class PreparePaymentUseCase(
    private val coinPackageAdaptor: CoinPackageAdaptor,
    private val enrollmentAdaptor: EnrollmentAdaptor,
    private val paymentAdaptor: PaymentAdaptor,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    @Transactional
    fun execute(
        userId: String,
        request: PreparePaymentRequest,
    ): PreparePaymentResponse {
        if (!enrollmentAdaptor.hasActiveMembershipEnrollment(userId, LocalDateTime.now(clock))) {
            throw NoActiveMembershipException()
        }
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
