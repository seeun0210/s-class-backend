package com.sclass.backoffice.webhook.dto

data class SurveyReportCallbackPayload(
    val event: String,
    val requestId: String,
    val sentAt: String,
    val result: Map<String, Any>? = null,
    val error: ErrorDetail? = null,
) {
    data class ErrorDetail(
        val code: String,
        val message: String,
        val retryable: Boolean,
    )
}
