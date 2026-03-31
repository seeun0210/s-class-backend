package com.sclass.supporters.commission.dto

import com.sclass.domain.domains.commission.domain.CommissionStatus
import jakarta.validation.constraints.NotNull

data class TransitionStatusRequest(
    @field:NotNull
    val status: CommissionStatus,

    val reason: String? = null,
)
