package com.sclass.backoffice.membershipproduct.usecase

import com.sclass.backoffice.membershipproduct.dto.CreateMembershipProductRequest
import com.sclass.backoffice.membershipproduct.dto.MembershipProductResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.coin.adaptor.CoinPackageAdaptor
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.MembershipProduct
import com.sclass.infrastructure.s3.ThumbnailUrlResolver
import org.springframework.transaction.annotation.Transactional

@UseCase
class CreateMembershipProductUseCase(
    private val productAdaptor: ProductAdaptor,
    private val coinPackageAdaptor: CoinPackageAdaptor,
    private val thumbnailUrlResolver: ThumbnailUrlResolver,
) {
    @Transactional
    fun execute(request: CreateMembershipProductRequest): MembershipProductResponse {
        val coinPackage = coinPackageAdaptor.findById(request.coinPackageId)
        val product =
            productAdaptor.save(
                MembershipProduct(
                    name = request.name,
                    priceWon = request.priceWon,
                    description = request.description,
                    thumbnailFileId = request.thumbnailFileId,
                    periodDays = request.periodDays,
                    maxEnrollments = request.maxEnrollments,
                    coinPackageId = request.coinPackageId,
                ),
            ) as MembershipProduct
        return MembershipProductResponse.from(
            product = product,
            thumbnailUrl = thumbnailUrlResolver.resolve(product.thumbnailFileId),
            coinAmount = coinPackage.coinAmount,
        )
    }
}
