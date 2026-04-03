package com.sclass.backoffice.webhook.event

data class SurveySubmittedNotificationEvent(
    val studentPhone: String?,
    val parentPhone: String?,
    val studentName: String,
    val submittedAt: String,
)

data class DiagnosisCompletedNotificationEvent(
    val studentPhone: String?,
    val parentPhone: String?,
    val studentName: String,
    val resultUrl: String,
)
