package com.sclass.supporters.enrollment.dto

import com.sclass.domain.domains.payment.domain.PgType
import jakarta.validation.constraints.NotNull

data class PrepareEnrollmentRequest(
    @field:NotNull val courseId: Long,
    @field:NotNull val pgType: PgType,
)
