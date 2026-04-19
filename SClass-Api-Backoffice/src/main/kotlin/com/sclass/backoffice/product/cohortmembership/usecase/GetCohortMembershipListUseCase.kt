package com.sclass.backoffice.product.cohortmembership.usecase

import com.sclass.backoffice.product.cohortmembership.dto.CohortMembershipPageResponse
import com.sclass.backoffice.product.cohortmembership.dto.CohortMembershipResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CohortMembershipProduct
import com.sclass.domain.domains.product.domain.ProductType
import com.sclass.infrastructure.s3.ThumbnailUrlResolver
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetCohortMembershipListUseCase(
    private val productAdaptor: ProductAdaptor,
    private val thumbnailUrlResolver: ThumbnailUrlResolver,
) {
    @Transactional(readOnly = true)
    fun execute(pageable: Pageable): CohortMembershipPageResponse {
        val page =
            productAdaptor.findMembershipsWithCoinPackage(
                type = ProductType.COHORT_MEMBERSHIP,
                visibleOnly = false,
                pageable = pageable,
            )
        val content =
            page.content.map { row ->
                CohortMembershipResponse.from(
                    product = row.product as CohortMembershipProduct,
                    thumbnailUrl = thumbnailUrlResolver.resolve(row.product.thumbnailFileId),
                    coinAmount = row.coinPackage?.coinAmount ?: 0,
                )
            }
        return CohortMembershipPageResponse(
            content = content,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            currentPage = page.number,
        )
    }
}
