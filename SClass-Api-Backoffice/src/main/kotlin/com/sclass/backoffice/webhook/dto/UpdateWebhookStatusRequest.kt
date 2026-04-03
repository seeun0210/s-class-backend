package com.sclass.backoffice.webhook.dto

import com.sclass.domain.domains.webhook.domain.WebhookStatus
import jakarta.validation.constraints.NotNull

data class UpdateWebhookStatusRequest(
    @field:NotNull
    val status: WebhookStatus,
)
