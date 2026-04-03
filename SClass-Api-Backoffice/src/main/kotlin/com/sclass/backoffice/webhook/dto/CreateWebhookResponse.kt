package com.sclass.backoffice.webhook.dto

import com.sclass.domain.domains.webhook.domain.WebhookType

data class CreateWebhookResponse(
    val id: String,
    val name: String,
    val type: WebhookType,
    val secret: String,
    val scriptCode: String,
    val fieldMapping: WebhookFieldMappingResponse,
)
