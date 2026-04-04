package com.sclass.backoffice.webhook.listenner

import com.sclass.backoffice.webhook.event.DiagnosisCompletedNotificationEvent
import com.sclass.backoffice.webhook.event.DiagnosisRequestedEvent
import com.sclass.backoffice.webhook.event.SurveyReportCompletedEvent
import com.sclass.backoffice.webhook.event.SurveyReportFailedEvent
import com.sclass.backoffice.webhook.event.SurveySubmittedNotificationEvent
import com.sclass.common.annotation.EventHandler
import com.sclass.domain.domains.diagnosis.adaptor.DiagnosisAdaptor
import com.sclass.domain.domains.diagnosis.domain.DiagnosisStatus
import com.sclass.infrastructure.report.ReportServiceClient
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import org.springframework.transaction.support.TransactionTemplate

@EventHandler
@ConditionalOnBean(ReportServiceClient::class)
class DiagnosisEventListener(
    private val diagnosisAdaptor: DiagnosisAdaptor,
    private val reportServiceClient: ReportServiceClient,
    private val eventPublisher: ApplicationEventPublisher,
    transactionManager: PlatformTransactionManager,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val tx = TransactionTemplate(transactionManager)

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleDiagnosisRequested(event: DiagnosisRequestedEvent) {
        val diagnosis = tx.execute { diagnosisAdaptor.findById(event.diagnosisId) }!!

        // 트랜잭션 밖: 외부 API 호출 (DB 커넥션 비점유, 논블로킹)
        reportServiceClient.createSurveyReport(
            requestId = event.requestId,
            studentName = event.studentName,
            answers = event.answers,
            callbackUrl = event.callbackUrl,
            callbackSecret = diagnosis.callbackSecret,
            onSuccess = {
                // 202 Accepted 후 PROCESSING 상태 변경 → 알림톡 발송
                // PENDING 상태 확인 후 변경 (콜백이 먼저 도착해 COMPLETED가 된 경우 덮어쓰지 않음)
                tx.execute {
                    val d = diagnosisAdaptor.findById(event.diagnosisId)
                    if (d.status == DiagnosisStatus.PENDING) {
                        d.markProcessing()
                        diagnosisAdaptor.save(d)
                    }
                }
                eventPublisher.publishEvent(
                    SurveySubmittedNotificationEvent(
                        studentPhone = diagnosis.studentPhone,
                        parentPhone = diagnosis.parentPhone,
                        studentName = event.studentName,
                        submittedAt = event.submittedAt,
                    ),
                )
            },
            onError = { e ->
                logger.error("[diagnosis] report-service 호출 실패 diagnosisId=${diagnosis.id}: ${e.message}")
                eventPublisher.publishEvent(
                    SurveyReportFailedEvent(
                        diagnosisId = diagnosis.id,
                        errorCode = "REPORT_SERVICE_ERROR",
                        retryable = true,
                    ),
                )
            },
        )
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleCompleted(event: SurveyReportCompletedEvent) {
        val resultUrl =
            tx.execute {
                val diagnosis = diagnosisAdaptor.findById(event.diagnosisId)
                diagnosis.complete(event.reportData)
                diagnosisAdaptor.save(diagnosis)
                diagnosis.resultUrl!!
            }!!

        eventPublisher.publishEvent(
            DiagnosisCompletedNotificationEvent(
                studentPhone = event.studentPhone,
                parentPhone = event.parentPhone,
                studentName = event.studentName,
                resultUrl = resultUrl,
            ),
        )
    }

    @Async
    @EventListener
    fun handleFailed(event: SurveyReportFailedEvent) {
        tx.execute {
            val diagnosis = diagnosisAdaptor.findById(event.diagnosisId)
            diagnosis.fail()
            diagnosisAdaptor.save(diagnosis)
        }
        logger.error("[diagnosis] 진단 실패 diagnosisId=${event.diagnosisId} errorCode=${event.errorCode} retryable=${event.retryable}")
    }
}
