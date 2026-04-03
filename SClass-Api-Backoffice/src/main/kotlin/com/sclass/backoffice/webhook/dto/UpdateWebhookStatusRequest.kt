package com.sclass.backoffice.webhook.dto

import com.sclass.domain.domains.webhook.domain.WebhookStatus

data class UpdateWebhookStatusRequest(
    val status: WebhookStatus,
)
