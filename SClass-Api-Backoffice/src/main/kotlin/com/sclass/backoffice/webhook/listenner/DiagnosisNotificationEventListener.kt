package com.sclass.backoffice.webhook.listenner

import com.sclass.backoffice.webhook.event.DiagnosisCompletedNotificationEvent
import com.sclass.backoffice.webhook.event.SurveySubmittedNotificationEvent
import com.sclass.common.annotation.EventHandler
import com.sclass.infrastructure.message.DiagnosisNotificationSender
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async

@EventHandler
class DiagnosisNotificationEventListener(
    private val diagnosisNotificationSender: DiagnosisNotificationSender,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Async
    @EventListener
    fun handle(event: SurveySubmittedNotificationEvent) {
        runCatching {
            event.studentPhone?.let {
                diagnosisNotificationSender.sendSurveySubmitted(it, event.studentName, event.submittedAt)
            }
            event.parentPhone?.let {
                diagnosisNotificationSender.sendSurveySubmittedToParent(it, event.studentName)
            }
        }.onFailure { logger.warn("[diagnosis] 설문 제출 알림톡 발송 실패 studentName=${event.studentName}: ${it.message}") }
    }

    @Async
    @EventListener
    fun handle(event: DiagnosisCompletedNotificationEvent) {
        runCatching {
            event.studentPhone?.let {
                diagnosisNotificationSender.sendDiagnosisCompleted(it, event.studentName, event.resultUrl)
            }
            event.parentPhone?.let {
                diagnosisNotificationSender.sendDiagnosisCompleted(it, event.studentName, event.resultUrl)
            }
        }.onFailure { logger.warn("[diagnosis] 진단 완료 알림톡 발송 실패 studentName=${event.studentName}: ${it.message}") }
    }
}
