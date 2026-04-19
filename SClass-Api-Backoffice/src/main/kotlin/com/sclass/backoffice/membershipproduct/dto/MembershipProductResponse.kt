package com.sclass.backoffice.membershipproduct.dto

import com.sclass.domain.domains.product.domain.MembershipProduct

data class MembershipProductResponse(
    val productId: String,
    val name: String,
    val description: String?,
    val thumbnailFileId: String?,
    val thumbnailUrl: String?,
    val priceWon: Int,
    val periodDays: Int,
    val maxEnrollments: Int?,
    val coinPackageId: String,
    val coinAmount: Int,
    val visible: Boolean,
) {
    companion object {
        fun from(
            product: MembershipProduct,
            thumbnailUrl: String?,
            coinAmount: Int,
        ) = MembershipProductResponse(
            productId = product.id,
            name = product.name,
            description = product.description,
            thumbnailFileId = product.thumbnailFileId,
            thumbnailUrl = thumbnailUrl,
            priceWon = product.priceWon,
            periodDays = product.periodDays,
            maxEnrollments = product.maxEnrollments,
            coinPackageId = product.coinPackageId,
            coinAmount = coinAmount,
            visible = product.visible,
        )
    }
}
