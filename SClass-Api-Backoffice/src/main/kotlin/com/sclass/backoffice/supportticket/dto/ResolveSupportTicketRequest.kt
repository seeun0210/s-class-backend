package com.sclass.backoffice.supportticket.dto

import jakarta.validation.constraints.NotBlank

data class ResolveSupportTicketRequest(
    @field:NotBlank
    val response: String,
)
