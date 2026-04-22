package com.sclass.backoffice.coin.usecase

import com.sclass.backoffice.coin.dto.AdminGrantCoinRequest
import com.sclass.backoffice.coin.dto.AdminGrantCoinResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.coin.domain.CoinLotSourceType
import com.sclass.domain.domains.coin.service.CoinDomainService
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.enrollment.exception.EnrollmentTypeMismatchException
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.MembershipProduct
import org.springframework.transaction.annotation.Transactional

@UseCase
class AdminGrantCoinUseCase(
    private val coinDomainService: CoinDomainService,
    private val enrollmentAdaptor: EnrollmentAdaptor,
    private val productAdaptor: ProductAdaptor,
) {
    @Transactional
    fun execute(
        adminUserId: String,
        request: AdminGrantCoinRequest,
    ): AdminGrantCoinResponse {
        request.enrollmentId?.let { id ->
            val enrollment = enrollmentAdaptor.findById(id)
            val productId = enrollment.productId ?: throw EnrollmentTypeMismatchException()
            if (productAdaptor.findById(productId) !is MembershipProduct) throw EnrollmentTypeMismatchException()
        }

        val lot =
            coinDomainService.issue(
                userId = request.studentUserId,
                amount = request.amount,
                description = "어드민 코인 지급 - ${request.grantReason}",
                enrollmentId = request.enrollmentId,
                expireAt = request.expireAt,
                sourceType = CoinLotSourceType.ADMIN_GRANT,
                sourceMeta = """{"grantedByUserId":"$adminUserId","reason":"${request.grantReason}"}""",
            )
        return AdminGrantCoinResponse.from(lot)
    }
}
