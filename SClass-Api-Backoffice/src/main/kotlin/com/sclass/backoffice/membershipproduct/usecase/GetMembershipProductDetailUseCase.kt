package com.sclass.backoffice.membershipproduct.usecase

import com.sclass.backoffice.membershipproduct.dto.MembershipProductResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.coin.adaptor.CoinPackageAdaptor
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.MembershipProduct
import com.sclass.domain.domains.product.exception.ProductTypeMismatchException
import com.sclass.infrastructure.s3.ThumbnailUrlResolver
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetMembershipProductDetailUseCase(
    private val productAdaptor: ProductAdaptor,
    private val coinPackageAdaptor: CoinPackageAdaptor,
    private val thumbnailUrlResolver: ThumbnailUrlResolver,
) {
    @Transactional(readOnly = true)
    fun execute(productId: String): MembershipProductResponse {
        val product =
            productAdaptor.findById(productId) as? MembershipProduct
                ?: throw ProductTypeMismatchException()
        val coinPackage = coinPackageAdaptor.findById(product.coinPackageId)
        return MembershipProductResponse.from(
            product = product,
            thumbnailUrl = thumbnailUrlResolver.resolve(product.thumbnailFileId),
            coinAmount = coinPackage.coinAmount,
        )
    }
}
