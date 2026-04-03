package com.sclass.backoffice.webhook.dto

import com.sclass.domain.domains.webhook.domain.Webhook
import com.sclass.domain.domains.webhook.domain.WebhookStatus
import com.sclass.domain.domains.webhook.domain.WebhookType
import java.time.LocalDateTime

data class WebhookResponse(
    val id: String,
    val name: String,
    val type: WebhookType,
    val status: WebhookStatus,
    val fieldMapping: WebhookFieldMappingResponse,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(webhook: Webhook): WebhookResponse =
            WebhookResponse(
                id = webhook.id,
                name = webhook.name,
                type = webhook.type,
                status = webhook.status,
                fieldMapping = WebhookFieldMappingResponse.from(webhook.fieldMapping),
                createdAt = webhook.createdAt,
            )
    }
}
