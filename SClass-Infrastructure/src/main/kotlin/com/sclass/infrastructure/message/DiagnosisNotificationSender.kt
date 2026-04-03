package com.sclass.infrastructure.message

interface DiagnosisNotificationSender {
    fun sendSurveySubmitted(
        phoneNumber: String,
        studentName: String,
        submittedAt: String,
    )

    fun sendSurveySubmittedToParent(
        phoneNumber: String,
        studentName: String,
    )

    fun sendDiagnosisCompleted(
        phoneNumber: String,
        studentName: String,
        resultUrl: String,
    )
}
