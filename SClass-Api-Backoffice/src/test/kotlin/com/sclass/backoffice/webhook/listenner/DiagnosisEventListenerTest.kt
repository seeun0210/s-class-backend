package com.sclass.backoffice.webhook.listenner

import com.sclass.backoffice.webhook.event.DiagnosisCompletedNotificationEvent
import com.sclass.backoffice.webhook.event.DiagnosisRequestedEvent
import com.sclass.backoffice.webhook.event.SurveyReportCompletedEvent
import com.sclass.backoffice.webhook.event.SurveyReportFailedEvent
import com.sclass.backoffice.webhook.event.SurveySubmittedNotificationEvent
import com.sclass.domain.domains.diagnosis.adaptor.DiagnosisAdaptor
import com.sclass.domain.domains.diagnosis.domain.Diagnosis
import com.sclass.domain.domains.diagnosis.domain.DiagnosisStatus
import com.sclass.infrastructure.report.ReportServiceClient
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.SimpleTransactionStatus

class DiagnosisEventListenerTest {
    private val diagnosisAdaptor = mockk<DiagnosisAdaptor>()
    private val reportServiceClient = mockk<ReportServiceClient>()
    private val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)

    private val transactionManager =
        object : PlatformTransactionManager {
            override fun getTransaction(definition: org.springframework.transaction.TransactionDefinition?): TransactionStatus =
                SimpleTransactionStatus()

            override fun commit(status: TransactionStatus) {}

            override fun rollback(status: TransactionStatus) {}
        }

    private val listener =
        DiagnosisEventListener(
            diagnosisAdaptor,
            reportServiceClient,
            eventPublisher,
            transactionManager,
        )

    private fun createDiagnosis(
        studentPhone: String? = "01012345678",
        parentPhone: String? = "01098765432",
    ) = Diagnosis(
        requestId = "req-001",
        studentName = "홍길동",
        studentPhone = studentPhone,
        parentPhone = parentPhone,
        requestData = "{}",
    )

    @Nested
    inner class HandleDiagnosisRequested {
        private val event =
            DiagnosisRequestedEvent(
                diagnosisId = "diag-001",
                requestId = "req-001",
                studentName = "홍길동",
                answers = mapOf("q1" to "a1"),
                callbackUrl = "https://callback.example.com",
                submittedAt = "2026-04-04 10:00:00",
            )

        @Test
        fun `API 호출 성공 시 PROCESSING으로 저장하고 알림 이벤트를 발행한다`() {
            val diagnosis = createDiagnosis()
            every { diagnosisAdaptor.findById(event.diagnosisId) } returns diagnosis
            every { diagnosisAdaptor.save(any()) } returns diagnosis
            every { reportServiceClient.createSurveyReport(any(), any(), any(), any(), any(), any(), any()) } answers {
                val onSuccess = arg<() -> Unit>(5)
                onSuccess()
            }

            listener.handleDiagnosisRequested(event)

            val notificationSlot = slot<SurveySubmittedNotificationEvent>()
            verify { eventPublisher.publishEvent(capture(notificationSlot)) }
            assertAll(
                { assertEquals(DiagnosisStatus.PROCESSING, diagnosis.status) },
                { verify { diagnosisAdaptor.save(diagnosis) } },
                {
                    verify {
                        reportServiceClient.createSurveyReport(
                            event.requestId,
                            event.studentName,
                            event.answers,
                            event.callbackUrl,
                            diagnosis.callbackSecret,
                            any(),
                            any(),
                        )
                    }
                },
                { assertEquals(diagnosis.studentPhone, notificationSlot.captured.studentPhone) },
                { assertEquals(diagnosis.parentPhone, notificationSlot.captured.parentPhone) },
                { assertEquals(event.submittedAt, notificationSlot.captured.submittedAt) },
            )
        }

        @Test
        fun `콜백이 먼저 도착해 COMPLETED 상태일 때 PROCESSING으로 덮어쓰지 않는다`() {
            val diagnosis = createDiagnosis()
            diagnosis.markProcessing()
            diagnosis.complete("{}")
            every { diagnosisAdaptor.findById(event.diagnosisId) } returns diagnosis
            every { diagnosisAdaptor.save(any()) } returns diagnosis
            every { reportServiceClient.createSurveyReport(any(), any(), any(), any(), any(), any(), any()) } answers {
                val onSuccess = arg<() -> Unit>(5)
                onSuccess()
            }

            listener.handleDiagnosisRequested(event)

            assertAll(
                { assertEquals(DiagnosisStatus.COMPLETED, diagnosis.status) },
                { verify(exactly = 0) { diagnosisAdaptor.save(diagnosis) } },
            )
        }

        @Test
        fun `API 호출 실패 시 SurveyReportFailedEvent를 발행하고 알림 이벤트를 발행하지 않는다`() {
            val diagnosis = createDiagnosis()
            every { diagnosisAdaptor.findById(event.diagnosisId) } returns diagnosis
            every { diagnosisAdaptor.save(any()) } returns diagnosis
            every { reportServiceClient.createSurveyReport(any(), any(), any(), any(), any(), any(), any()) } answers {
                val onError = arg<(Throwable) -> Unit>(6)
                onError(RuntimeException("서버 오류"))
            }

            listener.handleDiagnosisRequested(event)

            val eventSlot = slot<SurveyReportFailedEvent>()
            verify { eventPublisher.publishEvent(capture(eventSlot)) }
            assertAll(
                { assertEquals(diagnosis.id, eventSlot.captured.diagnosisId) },
                { assertEquals("REPORT_SERVICE_ERROR", eventSlot.captured.errorCode) },
                { assertEquals(true, eventSlot.captured.retryable) },
            )
            verify(exactly = 0) { eventPublisher.publishEvent(ofType<SurveySubmittedNotificationEvent>()) }
        }
    }

    @Nested
    inner class HandleCompleted {
        private val event =
            SurveyReportCompletedEvent(
                diagnosisId = "diag-001",
                studentName = "홍길동",
                studentPhone = "01012345678",
                parentPhone = "01098765432",
                reportData = "{\"score\":95}",
            )

        @Test
        fun `COMPLETED로 저장하고 알림 이벤트를 발행한다`() {
            val diagnosis = createDiagnosis()
            every { diagnosisAdaptor.findById(event.diagnosisId) } returns diagnosis
            every { diagnosisAdaptor.save(any()) } returns diagnosis

            listener.handleCompleted(event)

            val notificationSlot = slot<DiagnosisCompletedNotificationEvent>()
            verify { eventPublisher.publishEvent(capture(notificationSlot)) }
            assertAll(
                { assertEquals(DiagnosisStatus.COMPLETED, diagnosis.status) },
                { assertEquals(event.reportData, diagnosis.reportData) },
                { verify { diagnosisAdaptor.save(diagnosis) } },
                { assertEquals(event.studentPhone, notificationSlot.captured.studentPhone) },
                { assertEquals(event.parentPhone, notificationSlot.captured.parentPhone) },
                { assertEquals(diagnosis.resultUrl, notificationSlot.captured.resultUrl) },
            )
        }
    }

    @Nested
    inner class HandleFailed {
        @Test
        fun `FAILED로 저장한다`() {
            val diagnosis = createDiagnosis()
            val event = SurveyReportFailedEvent(diagnosisId = diagnosis.id, errorCode = "ERR_001", retryable = false)
            every { diagnosisAdaptor.findById(diagnosis.id) } returns diagnosis
            every { diagnosisAdaptor.save(any()) } returns diagnosis

            listener.handleFailed(event)

            assertAll(
                { assertEquals(DiagnosisStatus.FAILED, diagnosis.status) },
                { verify { diagnosisAdaptor.save(diagnosis) } },
            )
        }
    }
}
