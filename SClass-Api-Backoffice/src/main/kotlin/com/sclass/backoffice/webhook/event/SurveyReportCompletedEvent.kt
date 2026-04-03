package com.sclass.backoffice.webhook.event

data class SurveyReportCompletedEvent(
    val diagnosisId: String,
    val studentName: String,
    val studentPhone: String?,
    val parentPhone: String?,
    val reportData: String,
)
