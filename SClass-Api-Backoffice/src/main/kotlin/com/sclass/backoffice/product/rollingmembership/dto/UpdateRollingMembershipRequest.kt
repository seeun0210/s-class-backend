package com.sclass.backoffice.product.rollingmembership.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size

data class UpdateRollingMembershipRequest(
    @field:Size(max = 200)
    val name: String? = null,
    val description: String? = null,
    val thumbnailFileId: String? = null,
    @field:Min(0)
    val priceWon: Int? = null,
    @field:Min(1)
    val periodDays: Int? = null,
    @field:Min(0)
    val maxEnrollments: Int? = null,
    @field:Size(min = 1)
    val coinPackageId: String? = null,
)
