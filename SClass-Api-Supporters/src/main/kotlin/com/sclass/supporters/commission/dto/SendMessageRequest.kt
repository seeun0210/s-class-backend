package com.sclass.supporters.commission.dto

import jakarta.validation.constraints.NotBlank

data class SendMessageRequest(
    @field:NotBlank
    val content: String,
)
