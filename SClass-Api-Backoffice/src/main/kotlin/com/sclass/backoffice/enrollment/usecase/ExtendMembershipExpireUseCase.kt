package com.sclass.backoffice.enrollment.usecase

import com.sclass.backoffice.enrollment.dto.EnrollmentResponse
import com.sclass.backoffice.enrollment.dto.ExtendMembershipExpireRequest
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.coin.adaptor.CoinAdaptor
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.enrollment.exception.EnrollmentTypeMismatchException
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.MembershipProduct
import org.springframework.transaction.annotation.Transactional

@UseCase
class ExtendMembershipExpireUseCase(
    private val enrollmentAdaptor: EnrollmentAdaptor,
    private val coinAdaptor: CoinAdaptor,
    private val productAdaptor: ProductAdaptor,
) {
    @Transactional
    fun execute(
        enrollmentId: Long,
        request: ExtendMembershipExpireRequest,
    ): EnrollmentResponse {
        val enrollment = enrollmentAdaptor.findById(enrollmentId)
        val productId = enrollment.productId ?: throw EnrollmentTypeMismatchException()
        if (productAdaptor.findById(productId) !is MembershipProduct) throw EnrollmentTypeMismatchException()

        enrollment.endAt = request.newEndAt

        val lots = coinAdaptor.findLotsByEnrollmentId(enrollmentId)
        lots.forEach { it.expireAt = request.newEndAt }
        coinAdaptor.saveLots(lots)

        return EnrollmentResponse.from(enrollmentAdaptor.save(enrollment))
    }
}
