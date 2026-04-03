package com.sclass.backoffice.webhook.listenner

import com.sclass.backoffice.webhook.event.DiagnosisRequestedEvent
import com.sclass.backoffice.webhook.event.SurveyReportCompletedEvent
import com.sclass.backoffice.webhook.event.SurveyReportFailedEvent
import com.sclass.common.annotation.EventHandler
import com.sclass.domain.domains.diagnosis.adaptor.DiagnosisAdaptor
import com.sclass.infrastructure.message.DiagnosisNotificationSender
import com.sclass.infrastructure.report.ReportServiceClient
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.scheduling.annotation.Async
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@EventHandler
@ConditionalOnBean(ReportServiceClient::class)
class DiagnosisEventListener(
    private val diagnosisAdaptor: DiagnosisAdaptor,
    private val reportServiceClient: ReportServiceClient,
    private val diagnosisNotificationSender: DiagnosisNotificationSender,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handleDiagnosisRequested(event: DiagnosisRequestedEvent) {
        val diagnosis = diagnosisAdaptor.findById(event.diagnosisId)
        diagnosis.markProcessing()
        diagnosisAdaptor.save(diagnosis)

        reportServiceClient.createSurveyReport(
            requestId = event.requestId,
            studentName = event.studentName,
            answers = event.answers,
            callbackUrl = event.callbackUrl,
            callbackSecret = diagnosis.callbackSecret,
        )

        diagnosis.studentPhone?.let {
            diagnosisNotificationSender.sendSurveySubmitted(it, event.studentName, event.submittedAt)
        }
        diagnosis.parentPhone?.let {
            diagnosisNotificationSender.sendSurveySubmittedToParent(it, event.studentName)
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handleCompleted(event: SurveyReportCompletedEvent) {
        val diagnosis = diagnosisAdaptor.findById(event.diagnosisId)
        diagnosis.complete(event.reportData)
        diagnosisAdaptor.save(diagnosis)

        val resultUrl = diagnosis.resultUrl!!
        event.studentPhone?.let {
            diagnosisNotificationSender.sendDiagnosisCompleted(it, event.studentName, resultUrl)
        }
        event.parentPhone?.let {
            diagnosisNotificationSender.sendDiagnosisCompleted(it, event.studentName, resultUrl)
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handleFailed(event: SurveyReportFailedEvent) {
        val diagnosis = diagnosisAdaptor.findById(event.diagnosisId)
        diagnosis.fail()
        diagnosisAdaptor.save(diagnosis)
        logger.error("[diagnosis] 진단 실패 diagnosisId=${event.diagnosisId} errorCode=${event.errorCode} retryable=${event.retryable}")
    }
}
