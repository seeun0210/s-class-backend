package com.sclass.backoffice.enrollment.dto

import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

data class ExtendMembershipExpireRequest(
    @field:NotNull val newEndAt: LocalDateTime,
)
