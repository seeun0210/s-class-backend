package com.sclass.backoffice.product.cohortmembership.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class CreateCohortMembershipRequest(
    @field:NotBlank
    @field:Size(max = 200)
    val name: String,
    @field:Min(0)
    val priceWon: Int,
    @field:NotNull
    val startAt: LocalDateTime,
    @field:NotNull
    val endAt: LocalDateTime,
    val description: String? = null,
    val thumbnailFileId: String? = null,
    val maxEnrollments: Int? = null,
    @field:NotBlank
    val coinPackageId: String,
)
