package com.sclass.supporters.commission.dto

import com.sclass.domain.domains.commission.domain.SupportTicketType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateSupportTicketRequest(
    @field:NotNull
    val type: SupportTicketType,

    @field:NotBlank
    val reason: String,
)
