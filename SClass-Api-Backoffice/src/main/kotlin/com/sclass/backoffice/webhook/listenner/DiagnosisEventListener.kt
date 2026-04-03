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
import org.springframework.context.ApplicationEventPublisher
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.annotation.Async
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import org.springframework.transaction.support.TransactionTemplate

@EventHandler
@ConditionalOnBean(ReportServiceClient::class)
class DiagnosisEventListener(
    private val diagnosisAdaptor: DiagnosisAdaptor,
    private val reportServiceClient: ReportServiceClient,
    private val diagnosisNotificationSender: DiagnosisNotificationSender,
    private val eventPublisher: ApplicationEventPublisher,
    private val taskExecutor: TaskExecutor,
    transactionManager: PlatformTransactionManager,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val requiresNewTx =
        TransactionTemplate(transactionManager).apply {
            propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRES_NEW
        }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleDiagnosisRequested(event: DiagnosisRequestedEvent) {
        // 트랜잭션 1: PROCESSING 저장 후 즉시 커밋 (DB 커넥션 반환)
        val diagnosis =
            requiresNewTx.execute {
                val d = diagnosisAdaptor.findById(event.diagnosisId)
                d.markProcessing()
                diagnosisAdaptor.save(d)
                d
            }!!

        // 트랜잭션 밖: 외부 API 호출 (최대 30초, DB 커넥션 점유 없음)
        try {
            reportServiceClient.createSurveyReport(
                requestId = event.requestId,
                studentName = event.studentName,
                answers = event.answers,
                callbackUrl = event.callbackUrl,
                callbackSecret = diagnosis.callbackSecret,
            )
        } catch (e: Exception) {
            logger.error("[diagnosis] report-service 호출 실패 diagnosisId=${diagnosis.id}: ${e.message}")
            eventPublisher.publishEvent(
                SurveyReportFailedEvent(
                    diagnosisId = diagnosis.id,
                    errorCode = "REPORT_SERVICE_ERROR",
                    retryable = true,
                ),
            )
            return
        }

        // API 성공 후 알림톡 비동기 발송 (Spring TaskExecutor)
        taskExecutor.execute {
            runCatching {
                diagnosis.studentPhone?.let {
                    diagnosisNotificationSender.sendSurveySubmitted(it, event.studentName, event.submittedAt)
                }
                diagnosis.parentPhone?.let {
                    diagnosisNotificationSender.sendSurveySubmittedToParent(it, event.studentName)
                }
            }.onFailure { logger.warn("[diagnosis] 알림톡 발송 실패 diagnosisId=${diagnosis.id}: ${it.message}") }
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleCompleted(event: SurveyReportCompletedEvent) {
        val resultUrl =
            requiresNewTx.execute {
                val diagnosis = diagnosisAdaptor.findById(event.diagnosisId)
                diagnosis.complete(event.reportData)
                diagnosisAdaptor.save(diagnosis)
                diagnosis.resultUrl!!
            }!!

        taskExecutor.execute {
            runCatching {
                event.studentPhone?.let {
                    diagnosisNotificationSender.sendDiagnosisCompleted(it, event.studentName, resultUrl)
                }
                event.parentPhone?.let {
                    diagnosisNotificationSender.sendDiagnosisCompleted(it, event.studentName, resultUrl)
                }
            }.onFailure { logger.warn("[diagnosis] 알림톡 발송 실패 diagnosisId=${event.diagnosisId}: ${it.message}") }
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleFailed(event: SurveyReportFailedEvent) {
        requiresNewTx.execute {
            val diagnosis = diagnosisAdaptor.findById(event.diagnosisId)
            diagnosis.fail()
            diagnosisAdaptor.save(diagnosis)
        }
        logger.error("[diagnosis] 진단 실패 diagnosisId=${event.diagnosisId} errorCode=${event.errorCode} retryable=${event.retryable}")
    }
}
