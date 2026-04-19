package com.sclass.backoffice.membershipproduct.usecase

import com.sclass.backoffice.membershipproduct.dto.MembershipProductResponse
import com.sclass.backoffice.membershipproduct.dto.UpdateMembershipProductRequest
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.coin.adaptor.CoinPackageAdaptor
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.MembershipProduct
import com.sclass.domain.domains.product.exception.ProductTypeMismatchException
import com.sclass.infrastructure.s3.ThumbnailUrlResolver
import org.springframework.transaction.annotation.Transactional

@UseCase
class UpdateMembershipProductUseCase(
    private val productAdaptor: ProductAdaptor,
    private val coinPackageAdaptor: CoinPackageAdaptor,
    private val thumbnailUrlResolver: ThumbnailUrlResolver,
) {
    @Transactional
    fun execute(
        productId: String,
        request: UpdateMembershipProductRequest,
    ): MembershipProductResponse {
        val product =
            productAdaptor.findById(productId) as? MembershipProduct
                ?: throw ProductTypeMismatchException()

        request.coinPackageId?.let { coinPackageAdaptor.findById(it) }

        product.updateCatalog(
            newName = request.name,
            newDescription = request.description,
            newThumbnailFileId = request.thumbnailFileId,
            newPriceWon = request.priceWon,
        )
        product.updateMembership(
            newPeriodDays = request.periodDays,
            newMaxEnrollments = request.maxEnrollments,
            newCoinPackageId = request.coinPackageId,
        )

        val coinPackage = coinPackageAdaptor.findById(product.coinPackageId)
        return MembershipProductResponse.from(
            product = product,
            thumbnailUrl = thumbnailUrlResolver.resolve(product.thumbnailFileId),
            coinAmount = coinPackage.coinAmount,
        )
    }
}
