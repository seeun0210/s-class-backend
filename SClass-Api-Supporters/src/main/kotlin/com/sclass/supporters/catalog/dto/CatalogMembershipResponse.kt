package com.sclass.supporters.catalog.dto

data class CatalogMembershipResponse(
    val productId: String,
    val name: String,
    val description: String?,
    val thumbnailUrl: String?,
    val priceWon: Int,
    val periodDays: Int,
    val maxEnrollments: Int?,
    val remainingSeats: Long?,
    val coinAmount: Int,
)
