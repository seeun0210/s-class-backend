package com.sclass.backoffice.product.cohortmembership.dto

import com.sclass.domain.domains.product.domain.CohortMembershipProduct
import java.time.LocalDateTime

data class CohortMembershipResponse(
    val productId: String,
    val name: String,
    val description: String?,
    val thumbnailUrl: String?,
    val priceWon: Int,
    val visible: Boolean,
    val startAt: LocalDateTime,
    val endAt: LocalDateTime,
    val maxEnrollments: Int?,
    val coinPackageId: String,
    val coinAmount: Int,
) {
    companion object {
        fun from(
            product: CohortMembershipProduct,
            thumbnailUrl: String?,
            coinAmount: Int,
        ): CohortMembershipResponse =
            CohortMembershipResponse(
                productId = product.id,
                name = product.name,
                description = product.description,
                thumbnailUrl = thumbnailUrl,
                priceWon = product.priceWon,
                visible = product.visible,
                startAt = product.startAt,
                endAt = product.endAt,
                maxEnrollments = product.maxEnrollments,
                coinPackageId = product.coinPackageId,
                coinAmount = coinAmount,
            )
    }
}
