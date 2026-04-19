package com.sclass.backoffice.enrollment.dto

import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

data class GrantMembershipEnrollmentRequest(
    @field:NotBlank val studentUserId: String,
    @field:NotBlank val membershipProductId: String,
    @field:NotBlank val grantReason: String,
    val startAt: LocalDateTime? = null,
    val endAt: LocalDateTime? = null,
)
