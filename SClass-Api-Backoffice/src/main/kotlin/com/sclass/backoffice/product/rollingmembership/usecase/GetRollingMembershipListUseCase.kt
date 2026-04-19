package com.sclass.backoffice.product.rollingmembership.usecase

import com.sclass.backoffice.product.rollingmembership.dto.RollingMembershipPageResponse
import com.sclass.backoffice.product.rollingmembership.dto.RollingMembershipResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.ProductType
import com.sclass.domain.domains.product.domain.RollingMembershipProduct
import com.sclass.infrastructure.s3.ThumbnailUrlResolver
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetRollingMembershipListUseCase(
    private val productAdaptor: ProductAdaptor,
    private val thumbnailUrlResolver: ThumbnailUrlResolver,
) {
    @Transactional(readOnly = true)
    fun execute(pageable: Pageable): RollingMembershipPageResponse {
        val page =
            productAdaptor.findMembershipsWithCoinPackage(
                type = ProductType.ROLLING_MEMBERSHIP,
                visibleOnly = false,
                pageable = pageable,
            )
        val content =
            page.content.map { row ->
                RollingMembershipResponse.from(
                    product = row.product as RollingMembershipProduct,
                    thumbnailUrl = thumbnailUrlResolver.resolve(row.product.thumbnailFileId),
                    coinAmount = row.coinPackage?.coinAmount ?: 0,
                )
            }
        return RollingMembershipPageResponse(
            content = content,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            currentPage = page.number,
        )
    }
}
