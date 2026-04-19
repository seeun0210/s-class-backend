package com.sclass.backoffice.membershipproduct.usecase

import com.sclass.backoffice.membershipproduct.dto.MembershipProductPageResponse
import com.sclass.backoffice.membershipproduct.dto.MembershipProductResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.infrastructure.s3.ThumbnailUrlResolver
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetMembershipProductListUseCase(
    private val productAdaptor: ProductAdaptor,
    private val thumbnailUrlResolver: ThumbnailUrlResolver,
) {
    @Transactional(readOnly = true)
    fun execute(pageable: Pageable): MembershipProductPageResponse {
        val page = productAdaptor.findMembershipsWithCoinPackage(visibleOnly = false, pageable = pageable)
        val content =
            page.content.map { row ->
                MembershipProductResponse.from(
                    product = row.product,
                    thumbnailUrl = thumbnailUrlResolver.resolve(row.product.thumbnailFileId),
                    coinAmount = row.coinPackage?.coinAmount ?: 0,
                )
            }
        return MembershipProductPageResponse(
            content = content,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            currentPage = page.number,
        )
    }
}
