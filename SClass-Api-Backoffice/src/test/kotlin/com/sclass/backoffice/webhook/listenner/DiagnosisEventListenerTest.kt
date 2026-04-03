package com.sclass.backoffice.webhook.listenner

import com.sclass.backoffice.webhook.event.DiagnosisRequestedEvent
import com.sclass.backoffice.webhook.event.SurveyReportCompletedEvent
import com.sclass.backoffice.webhook.event.SurveyReportFailedEvent
import com.sclass.domain.domains.diagnosis.adaptor.DiagnosisAdaptor
import com.sclass.domain.domains.diagnosis.domain.Diagnosis
import com.sclass.domain.domains.diagnosis.domain.DiagnosisStatus
import com.sclass.infrastructure.message.DiagnosisNotificationSender
import com.sclass.infrastructure.report.ReportServiceClient
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher
import org.springframework.core.task.TaskExecutor
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.SimpleTransactionStatus

class DiagnosisEventListenerTest {
    private val diagnosisAdaptor = mockk<DiagnosisAdaptor>()
    private val reportServiceClient = mockk<ReportServiceClient>()
    private val notificationSender = mockk<DiagnosisNotificationSender>(relaxed = true)
    private val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)

    // 테스트에서는 동기 실행
    private val taskExecutor = TaskExecutor { it.run() }

    // 트랜잭션 없이 콜백만 실행하는 mock TransactionManager
    private val transactionManager =
        object : PlatformTransactionManager {
            override fun getTransaction(definition: TransactionDefinition?): TransactionStatus = SimpleTransactionStatus()

            override fun commit(status: TransactionStatus) {}

            override fun rollback(status: TransactionStatus) {}
        }

    private val listener =
        DiagnosisEventListener(
            diagnosisAdaptor,
            reportServiceClient,
            notificationSender,
            eventPublisher,
            taskExecutor,
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
        fun `API 호출 성공 시 PROCESSING으로 저장하고 알림톡을 발송한다`() {
            val diagnosis = createDiagnosis()
            every { diagnosisAdaptor.findById(event.diagnosisId) } returns diagnosis
            every { diagnosisAdaptor.save(any()) } returns diagnosis
            justRun { reportServiceClient.createSurveyReport(any(), any(), any(), any(), any()) }

            listener.handleDiagnosisRequested(event)

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
                        )
                    }
                },
                { verify { notificationSender.sendSurveySubmitted(diagnosis.studentPhone!!, event.studentName, event.submittedAt) } },
                { verify { notificationSender.sendSurveySubmittedToParent(diagnosis.parentPhone!!, event.studentName) } },
            )
        }

        @Test
        fun `API 호출 실패 시 SurveyReportFailedEvent를 발행하고 알림톡을 발송하지 않는다`() {
            val diagnosis = createDiagnosis()
            every { diagnosisAdaptor.findById(event.diagnosisId) } returns diagnosis
            every { diagnosisAdaptor.save(any()) } returns diagnosis
            every { reportServiceClient.createSurveyReport(any(), any(), any(), any(), any()) } throws RuntimeException("서버 오류")

            listener.handleDiagnosisRequested(event)

            val eventSlot = slot<SurveyReportFailedEvent>()
            verify { eventPublisher.publishEvent(capture(eventSlot)) }
            assertAll(
                { assertEquals(diagnosis.id, eventSlot.captured.diagnosisId) },
                { assertEquals("REPORT_SERVICE_ERROR", eventSlot.captured.errorCode) },
                { assertEquals(true, eventSlot.captured.retryable) },
            )
            verify(exactly = 0) { notificationSender.sendSurveySubmitted(any(), any(), any()) }
            verify(exactly = 0) { notificationSender.sendSurveySubmittedToParent(any(), any()) }
        }

        @Test
        fun `studentPhone이 없으면 학생 알림톡을 발송하지 않는다`() {
            val diagnosis = createDiagnosis(studentPhone = null)
            every { diagnosisAdaptor.findById(event.diagnosisId) } returns diagnosis
            every { diagnosisAdaptor.save(any()) } returns diagnosis
            justRun { reportServiceClient.createSurveyReport(any(), any(), any(), any(), any()) }

            listener.handleDiagnosisRequested(event)

            verify { notificationSender.sendSurveySubmittedToParent(diagnosis.parentPhone!!, event.studentName) }
            verify(exactly = 0) { notificationSender.sendSurveySubmitted(any(), any(), any()) }
        }

        @Test
        fun `parentPhone이 없으면 학부모 알림톡을 발송하지 않는다`() {
            val diagnosis = createDiagnosis(parentPhone = null)
            every { diagnosisAdaptor.findById(event.diagnosisId) } returns diagnosis
            every { diagnosisAdaptor.save(any()) } returns diagnosis
            justRun { reportServiceClient.createSurveyReport(any(), any(), any(), any(), any()) }

            listener.handleDiagnosisRequested(event)

            verify { notificationSender.sendSurveySubmitted(diagnosis.studentPhone!!, event.studentName, event.submittedAt) }
            verify(exactly = 0) { notificationSender.sendSurveySubmittedToParent(any(), any()) }
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
        fun `COMPLETED로 저장하고 학생과 학부모에게 알림톡을 발송한다`() {
            val diagnosis = createDiagnosis()
            every { diagnosisAdaptor.findById(event.diagnosisId) } returns diagnosis
            every { diagnosisAdaptor.save(any()) } returns diagnosis

            listener.handleCompleted(event)

            assertAll(
                { assertEquals(DiagnosisStatus.COMPLETED, diagnosis.status) },
                { assertEquals(event.reportData, diagnosis.reportData) },
                { verify { diagnosisAdaptor.save(diagnosis) } },
                { verify { notificationSender.sendDiagnosisCompleted(event.studentPhone!!, event.studentName, diagnosis.resultUrl!!) } },
                { verify { notificationSender.sendDiagnosisCompleted(event.parentPhone!!, event.studentName, diagnosis.resultUrl!!) } },
            )
        }

        @Test
        fun `studentPhone이 없으면 학생 알림톡을 발송하지 않는다`() {
            val diagnosis = createDiagnosis()
            every { diagnosisAdaptor.findById(event.diagnosisId) } returns diagnosis
            every { diagnosisAdaptor.save(any()) } returns diagnosis

            listener.handleCompleted(event.copy(studentPhone = null))

            verify { notificationSender.sendDiagnosisCompleted(event.parentPhone!!, event.studentName, any()) }
            verify(exactly = 0) { notificationSender.sendDiagnosisCompleted(event.studentPhone!!, any(), any()) }
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
