package com.sclass.backoffice.product.rollingmembership.dto

data class UpdateRollingMembershipRequest(
    val name: String? = null,
    val description: String? = null,
    val thumbnailFileId: String? = null,
    val priceWon: Int? = null,
    val periodDays: Int? = null,
    val maxEnrollments: Int? = null,
    val coinPackageId: String? = null,
)
