package com.sclass.backoffice.product.cohortmembership.usecase

import com.sclass.backoffice.product.cohortmembership.dto.CohortMembershipResponse
import com.sclass.backoffice.product.cohortmembership.dto.CreateCohortMembershipRequest
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.coin.adaptor.CoinPackageAdaptor
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CohortMembershipProduct
import com.sclass.infrastructure.s3.ThumbnailUrlResolver
import org.springframework.transaction.annotation.Transactional

@UseCase
class CreateCohortMembershipUseCase(
    private val productAdaptor: ProductAdaptor,
    private val coinPackageAdaptor: CoinPackageAdaptor,
    private val thumbnailUrlResolver: ThumbnailUrlResolver,
) {
    @Transactional
    fun execute(request: CreateCohortMembershipRequest): CohortMembershipResponse {
        val coinPackage = coinPackageAdaptor.findById(request.coinPackageId)
        val product =
            CohortMembershipProduct(
                name = request.name,
                priceWon = request.priceWon,
                description = request.description,
                thumbnailFileId = request.thumbnailFileId,
                maxEnrollments = request.maxEnrollments,
                coinPackageId = request.coinPackageId,
                startAt = request.startAt,
                endAt = request.endAt,
            )
        val saved = productAdaptor.save(product) as CohortMembershipProduct
        return CohortMembershipResponse.from(
            product = saved,
            thumbnailUrl = thumbnailUrlResolver.resolve(saved.thumbnailFileId),
            coinAmount = coinPackage.coinAmount,
        )
    }
}
