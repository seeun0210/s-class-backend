package com.sclass.infrastructure.message

interface DiagnosisNotificationSender {
    fun sendDiagnosisCompleted(
        phoneNumber: String,
        studentName: String,
        resultUrl: String,
    )
}
