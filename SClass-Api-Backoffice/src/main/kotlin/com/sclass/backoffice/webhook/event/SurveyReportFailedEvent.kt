package com.sclass.backoffice.webhook.event

data class SurveyReportFailedEvent(
    val diagnosisId: String,
    val errorCode: String?,
    val retryable: Boolean,
)
