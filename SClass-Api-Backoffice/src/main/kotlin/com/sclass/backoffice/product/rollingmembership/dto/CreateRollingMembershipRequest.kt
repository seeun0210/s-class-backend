package com.sclass.backoffice.product.rollingmembership.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateRollingMembershipRequest(
    @field:NotBlank
    @field:Size(max = 200)
    val name: String,
    @field:Min(0)
    val priceWon: Int,
    @field:Min(1)
    val periodDays: Int,
    val description: String? = null,
    val thumbnailFileId: String? = null,
    val maxEnrollments: Int? = null,
    @field:NotBlank
    val coinPackageId: String,
)
