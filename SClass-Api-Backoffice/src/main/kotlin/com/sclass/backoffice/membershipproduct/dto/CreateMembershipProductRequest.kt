package com.sclass.backoffice.membershipproduct.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateMembershipProductRequest(
    @field:NotBlank @field:Size(max = 200) val name: String,
    val description: String? = null,
    val thumbnailFileId: String? = null,
    @field:Min(0) val priceWon: Int,
    @field:Min(1) val periodDays: Int,
    @field:Min(1) val maxEnrollments: Int? = null,
    @field:NotBlank val coinPackageId: String,
)
