package com.sclass.supporters.catalog.dto

import com.sclass.domain.domains.product.domain.CohortMembershipProduct
import com.sclass.domain.domains.product.domain.MembershipProduct
import com.sclass.domain.domains.product.domain.ProductType
import com.sclass.domain.domains.product.domain.RollingMembershipProduct
import java.time.LocalDateTime

data class CatalogMembershipResponse(
    val productId: String,
    val productType: ProductType,
    val name: String,
    val description: String?,
    val thumbnailUrl: String?,
    val priceWon: Int,
    val periodDays: Int? = null,
    val startAt: LocalDateTime? = null,
    val endAt: LocalDateTime? = null,
    val maxEnrollments: Int?,
    val remainingSeats: Long?,
    val coinAmount: Int,
) {
    companion object {
        fun from(
            product: MembershipProduct,
            thumbnailUrl: String?,
            remainingSeats: Long?,
            coinAmount: Int,
        ): CatalogMembershipResponse =
            when (product) {
                is RollingMembershipProduct ->
                    CatalogMembershipResponse(
                        productId = product.id,
                        productType = ProductType.ROLLING_MEMBERSHIP,
                        name = product.name,
                        description = product.description,
                        thumbnailUrl = thumbnailUrl,
                        priceWon = product.priceWon,
                        periodDays = product.periodDays,
                        maxEnrollments = product.maxEnrollments,
                        remainingSeats = remainingSeats,
                        coinAmount = coinAmount,
                    )
                is CohortMembershipProduct ->
                    CatalogMembershipResponse(
                        productId = product.id,
                        productType = ProductType.COHORT_MEMBERSHIP,
                        name = product.name,
                        description = product.description,
                        thumbnailUrl = thumbnailUrl,
                        priceWon = product.priceWon,
                        startAt = product.startAt,
                        endAt = product.endAt,
                        maxEnrollments = product.maxEnrollments,
                        remainingSeats = remainingSeats,
                        coinAmount = coinAmount,
                    )
                else -> error("Unknown MembershipProduct subtype: ${product::class.simpleName}")
            }
    }
}
