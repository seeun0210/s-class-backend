package com.sclass.backoffice.product.rollingmembership.dto

import com.sclass.domain.domains.product.domain.RollingMembershipProduct

data class RollingMembershipResponse(
    val productId: String,
    val name: String,
    val description: String?,
    val thumbnailUrl: String?,
    val priceWon: Int,
    val visible: Boolean,
    val periodDays: Int,
    val maxEnrollments: Int?,
    val coinPackageId: String,
    val coinAmount: Int,
) {
    companion object {
        fun from(
            product: RollingMembershipProduct,
            thumbnailUrl: String?,
            coinAmount: Int,
        ): RollingMembershipResponse =
            RollingMembershipResponse(
                productId = product.id,
                name = product.name,
                description = product.description,
                thumbnailUrl = thumbnailUrl,
                priceWon = product.priceWon,
                visible = product.visible,
                periodDays = product.periodDays,
                maxEnrollments = product.maxEnrollments,
                coinPackageId = product.coinPackageId,
                coinAmount = coinAmount,
            )
    }
}
