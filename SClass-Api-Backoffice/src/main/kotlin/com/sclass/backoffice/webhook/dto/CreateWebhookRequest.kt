package com.sclass.backoffice.webhook.dto

import com.sclass.domain.domains.webhook.domain.WebhookType
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class CreateWebhookRequest(
    @field:NotBlank
    @field:Size(max = 100)
    val name: String,

    @field:NotNull
    val type: WebhookType,

    @field:Valid
    val fieldMapping: WebhookFieldMappingRequest,
)
