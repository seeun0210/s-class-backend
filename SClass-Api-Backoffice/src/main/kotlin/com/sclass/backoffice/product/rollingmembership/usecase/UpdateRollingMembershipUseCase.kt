package com.sclass.backoffice.product.rollingmembership.usecase

import com.sclass.backoffice.product.rollingmembership.dto.RollingMembershipResponse
import com.sclass.backoffice.product.rollingmembership.dto.UpdateRollingMembershipRequest
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.coin.adaptor.CoinPackageAdaptor
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.RollingMembershipProduct
import com.sclass.domain.domains.product.exception.ProductTypeMismatchException
import com.sclass.infrastructure.s3.ThumbnailUrlResolver
import org.springframework.transaction.annotation.Transactional

@UseCase
class UpdateRollingMembershipUseCase(
    private val productAdaptor: ProductAdaptor,
    private val coinPackageAdaptor: CoinPackageAdaptor,
    private val thumbnailUrlResolver: ThumbnailUrlResolver,
) {
    @Transactional
    fun execute(
        productId: String,
        request: UpdateRollingMembershipRequest,
    ): RollingMembershipResponse {
        val product =
            productAdaptor.findById(productId) as? RollingMembershipProduct
                ?: throw ProductTypeMismatchException()

        val newCoinPackage = request.coinPackageId?.let { coinPackageAdaptor.findById(it) }

        product.updateCatalog(
            newName = request.name,
            newDescription = request.description,
            newThumbnailFileId = request.thumbnailFileId,
            newPriceWon = request.priceWon,
        )
        product.updateMembershipShared(
            newMaxEnrollments = request.maxEnrollments,
            newCoinPackageId = request.coinPackageId,
        )
        product.updateRolling(newPeriodDays = request.periodDays)

        val coinPackage = newCoinPackage ?: coinPackageAdaptor.findById(product.coinPackageId)
        return RollingMembershipResponse.from(
            product = product,
            thumbnailUrl = thumbnailUrlResolver.resolve(product.thumbnailFileId),
            coinAmount = coinPackage.coinAmount,
        )
    }
}
