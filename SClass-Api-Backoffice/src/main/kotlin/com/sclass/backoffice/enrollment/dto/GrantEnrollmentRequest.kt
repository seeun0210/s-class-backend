package com.sclass.backoffice.enrollment.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class GrantEnrollmentRequest(
    @field:NotNull val courseId: Long,
    @field:NotBlank val studentUserId: String,
    @field:NotBlank val grantReason: String,
)
