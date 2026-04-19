package com.sclass.backoffice.product.cohortmembership.usecase

import com.sclass.backoffice.product.cohortmembership.dto.CohortMembershipResponse
import com.sclass.backoffice.product.cohortmembership.dto.UpdateCohortMembershipRequest
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.coin.adaptor.CoinPackageAdaptor
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CohortMembershipProduct
import com.sclass.domain.domains.product.exception.ProductTypeMismatchException
import com.sclass.infrastructure.s3.ThumbnailUrlResolver
import org.springframework.transaction.annotation.Transactional

@UseCase
class UpdateCohortMembershipUseCase(
    private val productAdaptor: ProductAdaptor,
    private val coinPackageAdaptor: CoinPackageAdaptor,
    private val thumbnailUrlResolver: ThumbnailUrlResolver,
) {
    @Transactional
    fun execute(
        productId: String,
        request: UpdateCohortMembershipRequest,
    ): CohortMembershipResponse {
        val product =
            productAdaptor.findById(productId) as? CohortMembershipProduct
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
        product.updateCohort(
            newStartAt = request.startAt,
            newEndAt = request.endAt,
        )

        val coinPackage = newCoinPackage ?: coinPackageAdaptor.findById(product.coinPackageId)
        return CohortMembershipResponse.from(
            product = product,
            thumbnailUrl = thumbnailUrlResolver.resolve(product.thumbnailFileId),
            coinAmount = coinPackage.coinAmount,
        )
    }
}
