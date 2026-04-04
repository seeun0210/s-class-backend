package com.sclass.infrastructure.report.dto

data class CreateSurveyReportRequest(
    val requestId: String,
    val studentName: String,
    val answers: Map<String, Any>,
    val callback: CallbackConfig,
)

data class CallbackConfig(
    val url: String,
    val secret: String,
)
