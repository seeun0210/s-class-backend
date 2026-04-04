package com.sclass.backoffice.webhook.event

data class DiagnosisRequestedEvent(
    val diagnosisId: String,
    val requestId: String,
    val studentName: String,
    val answers: Map<String, Any>,
    val callbackUrl: String,
    val submittedAt: String,
)
