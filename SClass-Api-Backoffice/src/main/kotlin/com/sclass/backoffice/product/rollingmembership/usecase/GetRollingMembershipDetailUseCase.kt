package com.sclass.backoffice.product.rollingmembership.usecase

import com.sclass.backoffice.product.rollingmembership.dto.RollingMembershipResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.coin.adaptor.CoinPackageAdaptor
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.RollingMembershipProduct
import com.sclass.domain.domains.product.exception.ProductTypeMismatchException
import com.sclass.infrastructure.s3.ThumbnailUrlResolver
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetRollingMembershipDetailUseCase(
    private val productAdaptor: ProductAdaptor,
    private val coinPackageAdaptor: CoinPackageAdaptor,
    private val thumbnailUrlResolver: ThumbnailUrlResolver,
) {
    @Transactional(readOnly = true)
    fun execute(productId: String): RollingMembershipResponse {
        val product =
            productAdaptor.findById(productId) as? RollingMembershipProduct
                ?: throw ProductTypeMismatchException()
        val coinPackage = coinPackageAdaptor.findById(product.coinPackageId)
        return RollingMembershipResponse.from(
            product = product,
            thumbnailUrl = thumbnailUrlResolver.resolve(product.thumbnailFileId),
            coinAmount = coinPackage.coinAmount,
        )
    }
}
