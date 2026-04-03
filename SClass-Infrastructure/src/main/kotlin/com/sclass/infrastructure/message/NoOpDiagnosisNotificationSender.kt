package com.sclass.infrastructure.message

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.stereotype.Component

@Component
@ConditionalOnMissingBean(DiagnosisNotificationSender::class)
class NoOpDiagnosisNotificationSender : DiagnosisNotificationSender {
    override fun sendSurveySubmitted(
        phoneNumber: String,
        studentName: String,
        submittedAt: String,
    ) = Unit

    override fun sendSurveySubmittedToParent(
        phoneNumber: String,
        studentName: String,
    ) = Unit

    override fun sendDiagnosisCompleted(
        phoneNumber: String,
        studentName: String,
        resultUrl: String,
    ) = Unit
}
