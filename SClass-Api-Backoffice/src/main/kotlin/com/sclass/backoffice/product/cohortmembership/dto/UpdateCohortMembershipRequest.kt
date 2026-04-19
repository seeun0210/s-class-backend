package com.sclass.backoffice.product.cohortmembership.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class UpdateCohortMembershipRequest(
    @field:Size(max = 200)
    val name: String? = null,
    val description: String? = null,
    val thumbnailFileId: String? = null,
    @field:Min(0)
    val priceWon: Int? = null,
    val startAt: LocalDateTime? = null,
    val endAt: LocalDateTime? = null,
    @field:Min(0)
    val maxEnrollments: Int? = null,
    @field:Size(min = 1)
    val coinPackageId: String? = null,
)
