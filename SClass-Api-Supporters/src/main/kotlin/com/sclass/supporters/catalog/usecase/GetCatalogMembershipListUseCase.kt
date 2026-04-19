package com.sclass.supporters.catalog.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.coin.adaptor.CoinPackageAdaptor
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.infrastructure.s3.ThumbnailUrlResolver
import com.sclass.supporters.catalog.dto.CatalogMembershipPageResponse
import com.sclass.supporters.catalog.dto.CatalogMembershipResponse
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetCatalogMembershipListUseCase(
    private val productAdaptor: ProductAdaptor,
    private val coinPackageAdaptor: CoinPackageAdaptor,
    private val enrollmentAdaptor: EnrollmentAdaptor,
    private val thumbnailUrlResolver: ThumbnailUrlResolver,
) {
    @Transactional(readOnly = true)
    fun execute(pageable: Pageable): CatalogMembershipPageResponse {
        val page = productAdaptor.findVisibleMemberships(pageable)
        val content =
            page.content.map { product ->
                val coinPackage = coinPackageAdaptor.findById(product.coinPackageId)
                val remaining =
                    product.maxEnrollments?.let { cap ->
                        val live = enrollmentAdaptor.countLiveMembershipEnrollments(product.id)
                        (cap - live).coerceAtLeast(0L)
                    }
                CatalogMembershipResponse(
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
        return CatalogMembershipPageResponse(
            content = content,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            currentPage = page.number,
        )
    }
}
