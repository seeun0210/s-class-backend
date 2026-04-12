package com.sclass.infrastructure.report.dto

data class ReportCallbackPayload(
    val event: String? = null,
    val jobId: String? = null,
    val reportId: String? = null,
    val sentAt: String? = null,
    val result: Map<String, Any?>? = null,
    val error: ErrorDetail? = null,
) {
    data class ErrorDetail(
        val code: String? = null,
        val message: String? = null,
        val retryable: Boolean = false,
    )
}
