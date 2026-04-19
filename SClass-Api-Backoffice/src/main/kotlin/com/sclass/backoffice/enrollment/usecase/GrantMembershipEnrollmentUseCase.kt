package com.sclass.backoffice.enrollment.usecase

import com.sclass.backoffice.enrollment.dto.EnrollmentResponse
import com.sclass.backoffice.enrollment.dto.GrantMembershipEnrollmentRequest
import com.sclass.common.annotation.UseCase
import com.sclass.common.exception.BusinessException
import com.sclass.common.exception.GlobalErrorCode
import com.sclass.domain.domains.coin.adaptor.CoinPackageAdaptor
import com.sclass.domain.domains.coin.domain.CoinLotSourceType
import com.sclass.domain.domains.coin.service.CoinDomainService
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.enrollment.domain.Enrollment
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.ActivePeriod
import com.sclass.domain.domains.product.domain.MembershipProduct
import com.sclass.domain.domains.product.exception.ProductTypeMismatchException
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDateTime

@UseCase
class GrantMembershipEnrollmentUseCase(
    private val productAdaptor: ProductAdaptor,
    private val coinPackageAdaptor: CoinPackageAdaptor,
    private val enrollmentAdaptor: EnrollmentAdaptor,
    private val coinDomainService: CoinDomainService,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    @Transactional
    fun execute(
        adminUserId: String,
        request: GrantMembershipEnrollmentRequest,
    ): EnrollmentResponse {
        val product =
            productAdaptor.findById(request.membershipProductId) as? MembershipProduct
                ?: throw ProductTypeMismatchException()
        val coinPackage = coinPackageAdaptor.findById(product.coinPackageId)

        val hasStartAt = request.startAt != null
        val hasEndAt = request.endAt != null
        if (hasStartAt xor hasEndAt) throw BusinessException(GlobalErrorCode.INVALID_INPUT)

        val now = LocalDateTime.now(clock)
        val period =
            if (hasStartAt && hasEndAt) {
                ActivePeriod(startAt = request.startAt!!, endAt = request.endAt!!)
            } else {
                product.resolveActivePeriod(now)
            }

        val enrollment =
            enrollmentAdaptor.save(
                Enrollment.createMembershipByGrant(
                    productId = product.id,
                    studentUserId = request.studentUserId,
                    grantedByUserId = adminUserId,
                    grantReason = request.grantReason,
                    startAt = period.startAt,
                    endAt = period.endAt,
                ),
            )

        coinDomainService.issue(
            userId = request.studentUserId,
            amount = coinPackage.coinAmount,
            referenceId = enrollment.id.toString(),
            description = "어드민 멤버십 부여 - ${product.name}",
            enrollmentId = enrollment.id,
            expireAt = period.endAt,
            sourceType = CoinLotSourceType.ADMIN_GRANT,
            sourceMeta = """{"membershipProductId":"${product.id}"}""",
        )

        return EnrollmentResponse.from(enrollment)
    }
}
