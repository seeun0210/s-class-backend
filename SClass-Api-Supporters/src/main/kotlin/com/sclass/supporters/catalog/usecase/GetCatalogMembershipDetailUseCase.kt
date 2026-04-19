package com.sclass.supporters.catalog.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.coin.adaptor.CoinPackageAdaptor
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.MembershipProduct
import com.sclass.domain.domains.product.exception.ProductNotFoundException
import com.sclass.domain.domains.product.exception.ProductTypeMismatchException
import com.sclass.infrastructure.s3.ThumbnailUrlResolver
import com.sclass.supporters.catalog.dto.CatalogMembershipResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetCatalogMembershipDetailUseCase(
    private val productAdaptor: ProductAdaptor,
    private val coinPackageAdaptor: CoinPackageAdaptor,
    private val enrollmentAdaptor: EnrollmentAdaptor,
    private val thumbnailUrlResolver: ThumbnailUrlResolver,
) {
    @Transactional(readOnly = true)
    fun execute(productId: String): CatalogMembershipResponse {
        val product =
            productAdaptor.findById(productId) as? MembershipProduct
                ?: throw ProductTypeMismatchException()
        if (!product.visible) throw ProductNotFoundException()

        val coinPackage = coinPackageAdaptor.findById(product.coinPackageId)
        val remaining =
            product.maxEnrollments?.let { cap ->
                val live = enrollmentAdaptor.countLiveMembershipEnrollments(product.id)
                (cap - live).coerceAtLeast(0L)
            }
        return CatalogMembershipResponse(
            productId = product.id,
            name = product.name,
            description = product.description,
            thumbnailUrl = thumbnailUrlResolver.resolve(product.thumbnailFileId),
            priceWon = product.priceWon,
            periodDays = product.periodDays,
            maxEnrollments = product.maxEnrollments,
            remainingSeats = remaining,
            coinAmount = coinPackage.coinAmount,
        )
    }
}
