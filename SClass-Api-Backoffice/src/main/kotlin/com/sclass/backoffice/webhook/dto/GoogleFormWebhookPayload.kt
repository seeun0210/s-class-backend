package com.sclass.backoffice.webhook.dto

data class GoogleFormWebhookPayload(
    val formId: String?,
    val formTitle: String?,
    val formResponseId: String,
    val submittedAt: String,
    val answers: Map<String, Any>,
)
