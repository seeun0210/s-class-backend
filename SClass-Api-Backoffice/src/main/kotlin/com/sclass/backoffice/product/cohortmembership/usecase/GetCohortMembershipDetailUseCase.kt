package com.sclass.backoffice.product.cohortmembership.usecase

import com.sclass.backoffice.product.cohortmembership.dto.CohortMembershipResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.coin.adaptor.CoinPackageAdaptor
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CohortMembershipProduct
import com.sclass.domain.domains.product.exception.ProductTypeMismatchException
import com.sclass.infrastructure.s3.ThumbnailUrlResolver
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetCohortMembershipDetailUseCase(
    private val productAdaptor: ProductAdaptor,
    private val coinPackageAdaptor: CoinPackageAdaptor,
    private val thumbnailUrlResolver: ThumbnailUrlResolver,
) {
    @Transactional(readOnly = true)
    fun execute(productId: String): CohortMembershipResponse {
        val product =
            productAdaptor.findById(productId) as? CohortMembershipProduct
                ?: throw ProductTypeMismatchException()
        val coinPackage = coinPackageAdaptor.findById(product.coinPackageId)
        return CohortMembershipResponse.from(
            product = product,
            thumbnailUrl = thumbnailUrlResolver.resolve(product.thumbnailFileId),
            coinAmount = coinPackage.coinAmount,
        )
    }
}
