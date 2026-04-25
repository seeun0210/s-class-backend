package com.sclass.supporters.enrollment.dto

import com.sclass.domain.domains.payment.domain.PgType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class PrepareEnrollmentRequest(
    @field:NotBlank val productId: String,
    @field:NotNull val pgType: PgType,
    val courseId: Long?,
)
