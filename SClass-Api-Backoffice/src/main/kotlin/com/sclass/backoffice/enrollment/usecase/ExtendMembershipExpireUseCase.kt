package com.sclass.backoffice.enrollment.usecase

import com.sclass.backoffice.enrollment.dto.EnrollmentResponse
import com.sclass.backoffice.enrollment.dto.ExtendMembershipExpireRequest
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.coin.adaptor.CoinAdaptor
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.enrollment.exception.EnrollmentTypeMismatchException
import org.springframework.transaction.annotation.Transactional

@UseCase
class ExtendMembershipExpireUseCase(
    private val enrollmentAdaptor: EnrollmentAdaptor,
    private val coinAdaptor: CoinAdaptor,
) {
    @Transactional
    fun execute(
        enrollmentId: Long,
        request: ExtendMembershipExpireRequest,
    ): EnrollmentResponse {
        val enrollment = enrollmentAdaptor.findById(enrollmentId)
        if (enrollment.productId == null) throw EnrollmentTypeMismatchException()

        enrollment.endAt = request.newEndAt

        val lots = coinAdaptor.findLotsByEnrollmentId(enrollmentId)
        lots.forEach { it.expireAt = request.newEndAt }
        coinAdaptor.saveLots(lots)

        return EnrollmentResponse.from(enrollmentAdaptor.save(enrollment))
    }
}
