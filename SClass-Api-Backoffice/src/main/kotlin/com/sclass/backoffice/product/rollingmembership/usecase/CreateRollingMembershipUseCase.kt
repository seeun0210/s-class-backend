package com.sclass.backoffice.product.rollingmembership.usecase

import com.sclass.backoffice.product.rollingmembership.dto.CreateRollingMembershipRequest
import com.sclass.backoffice.product.rollingmembership.dto.RollingMembershipResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.coin.adaptor.CoinPackageAdaptor
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.RollingMembershipProduct
import com.sclass.infrastructure.s3.ThumbnailUrlResolver
import org.springframework.transaction.annotation.Transactional

@UseCase
class CreateRollingMembershipUseCase(
    private val productAdaptor: ProductAdaptor,
    private val coinPackageAdaptor: CoinPackageAdaptor,
    private val thumbnailUrlResolver: ThumbnailUrlResolver,
) {
    @Transactional
    fun execute(request: CreateRollingMembershipRequest): RollingMembershipResponse {
        val coinPackage = coinPackageAdaptor.findById(request.coinPackageId)
        val product =
            RollingMembershipProduct(
                name = request.name,
                priceWon = request.priceWon,
                description = request.description,
                thumbnailFileId = request.thumbnailFileId,
                maxEnrollments = request.maxEnrollments,
                coinPackageId = request.coinPackageId,
                periodDays = request.periodDays,
            )
        val saved = productAdaptor.save(product) as RollingMembershipProduct
        return RollingMembershipResponse.from(
            product = saved,
            thumbnailUrl = thumbnailUrlResolver.resolve(saved.thumbnailFileId),
            coinAmount = coinPackage.coinAmount,
        )
    }
}
