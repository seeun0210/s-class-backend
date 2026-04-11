package com.sclass.infrastructure.report.dto

data class ReportCallbackPayload(
    val event: String,
    val requestId: String,
    val sentAt: String,
    val result: ReportResult? = null,
    val error: ErrorDetail? = null,
) {
    data class ReportResult(
        val jobId: String,
        val topic: String?,
    )

    data class ErrorDetail(
        val code: String,
        val message: String,
        val retryable: Boolean,
    )
}
