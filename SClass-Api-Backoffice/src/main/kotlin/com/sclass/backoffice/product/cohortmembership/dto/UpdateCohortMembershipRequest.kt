package com.sclass.backoffice.product.cohortmembership.dto

import java.time.LocalDateTime

data class UpdateCohortMembershipRequest(
    val name: String? = null,
    val description: String? = null,
    val thumbnailFileId: String? = null,
    val priceWon: Int? = null,
    val startAt: LocalDateTime? = null,
    val endAt: LocalDateTime? = null,
    val maxEnrollments: Int? = null,
    val coinPackageId: String? = null,
)
