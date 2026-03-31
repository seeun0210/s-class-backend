package com.sclass.supporters.commission.dto

import com.sclass.domain.domains.commission.domain.MessageType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class SendMessageRequest(
    @field:NotNull
    val type: MessageType,

    @field:NotBlank
    val content: String,
)
