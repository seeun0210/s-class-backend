package com.sclass.supporters.catalog.usecase

import com.sclass.common.annotation.UseCase
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
    private val enrollmentAdaptor: EnrollmentAdaptor,
    private val thumbnailUrlResolver: ThumbnailUrlResolver,
) {
    @Transactional(readOnly = true)
    fun execute(pageable: Pageable): CatalogMembershipPageResponse {
        val page = productAdaptor.findMembershipsWithCoinPackage(visibleOnly = true, pageable = pageable)

        val productIdsWithCap = page.content.filter { it.product.maxEnrollments != null }.map { it.product.id }
        val liveCounts =
            if (productIdsWithCap.isNotEmpty()) {
                enrollmentAdaptor.countLiveMembershipEnrollmentsByProductIds(productIdsWithCap)
            } else {
                emptyMap()
            }

        val content =
            page.content.map { row ->
                val product = row.product
                val remaining =
                    product.maxEnrollments?.let { cap ->
                        val live = liveCounts[product.id] ?: 0L
                        (cap - live).coerceAtLeast(0L)
                    }
                CatalogMembershipResponse.from(
                    product = product,
                    thumbnailUrl = thumbnailUrlResolver.resolve(product.thumbnailFileId),
                    remainingSeats = remaining,
                    coinAmount = row.coinPackage?.coinAmount ?: 0,
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
