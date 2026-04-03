package com.sclass.backoffice.webhook.usecase

import com.sclass.backoffice.webhook.dto.SurveyReportCallbackPayload
import com.sclass.backoffice.webhook.event.SurveyReportCompletedEvent
import com.sclass.backoffice.webhook.event.SurveyReportFailedEvent
import com.sclass.domain.domains.diagnosis.adaptor.DiagnosisAdaptor
import com.sclass.domain.domains.diagnosis.domain.Diagnosis
import com.sclass.domain.domains.diagnosis.domain.DiagnosisStatus
import com.sclass.domain.domains.webhook.exception.WebhookInvalidSecretException
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher
import tools.jackson.databind.ObjectMapper
import java.time.Instant
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class ReceiveReportWebhookUseCaseTest {
    private val diagnosisAdaptor = mockk<DiagnosisAdaptor>()
    private val objectMapper = mockk<ObjectMapper>()
    private val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
    private val useCase = ReceiveReportWebhookUseCase(diagnosisAdaptor, objectMapper, eventPublisher)

    private val callbackSecret = "test-callback-secret"
    private val requestId = "req-001"
    private val rawBody = """{"event":"survey_report.completed","requestId":"$requestId","sentAt":"2026-04-03T10:00:00Z"}"""

    private fun currentTimestamp() = Instant.now().epochSecond.toString()

    private fun generateSignature(
        secret: String,
        timestamp: String,
        body: String,
    ): String =
        Mac
            .getInstance("HmacSHA256")
            .apply { init(SecretKeySpec(secret.toByteArray(), "HmacSHA256")) }
            .doFinal("$timestamp.$body".toByteArray())
            .joinToString("", prefix = "sha256=") { "%02x".format(it) }

    private fun createDiagnosis(status: DiagnosisStatus = DiagnosisStatus.PENDING) =
        Diagnosis(
            requestId = requestId,
            studentName = "홍길동",
            studentPhone = "010-1234-5678",
            parentPhone = "010-9876-5432",
            requestData = "{}",
            callbackSecret = callbackSecret,
        ).also {
            if (status == DiagnosisStatus.COMPLETED) it.complete("{}")
            if (status == DiagnosisStatus.FAILED) it.fail()
        }

    @Nested
    inner class TimestampVerification {
        @Test
        fun `만료된 타임스탬프이면 WebhookInvalidSecretException을 던진다`() {
            val expiredTimestamp = (Instant.now().epochSecond - 400).toString()
            val signature = generateSignature(callbackSecret, expiredTimestamp, rawBody)

            assertThrows(WebhookInvalidSecretException::class.java) {
                useCase.execute(signature, expiredTimestamp, "survey_report.completed", rawBody)
            }
        }

        @Test
        fun `숫자가 아닌 타임스탬프이면 WebhookInvalidSecretException을 던진다`() {
            assertThrows(WebhookInvalidSecretException::class.java) {
                useCase.execute("sha256=abc", "not-a-number", "survey_report.completed", rawBody)
            }
        }
    }

    @Nested
    inner class SignatureVerification {
        @Test
        fun `잘못된 서명이면 WebhookInvalidSecretException을 던진다`() {
            val timestamp = currentTimestamp()
            val diagnosis = createDiagnosis()
            val completedPayload =
                SurveyReportCallbackPayload(
                    event = "survey_report.completed",
                    requestId = requestId,
                    sentAt = "2026-04-03T10:00:00Z",
                    result = mapOf("score" to 95),
                )
            every { diagnosisAdaptor.findByRequestId(requestId) } returns diagnosis
            every { objectMapper.readValue(rawBody, SurveyReportCallbackPayload::class.java) } returns completedPayload

            assertThrows(WebhookInvalidSecretException::class.java) {
                useCase.execute("sha256=invalidsignature", timestamp, "survey_report.completed", rawBody)
            }
        }
    }

    @Nested
    inner class Idempotency {
        @Test
        fun `이미 COMPLETED 상태이면 이벤트를 발행하지 않는다`() {
            val timestamp = currentTimestamp()
            val diagnosis = createDiagnosis(DiagnosisStatus.COMPLETED)
            val signature = generateSignature(callbackSecret, timestamp, rawBody)
            val payload =
                SurveyReportCallbackPayload(
                    event = "survey_report.completed",
                    requestId = requestId,
                    sentAt = "2026-04-03T10:00:00Z",
                )
            every { diagnosisAdaptor.findByRequestId(requestId) } returns diagnosis
            every { objectMapper.readValue(rawBody, SurveyReportCallbackPayload::class.java) } returns payload

            useCase.execute(signature, timestamp, "survey_report.completed", rawBody)

            verify(exactly = 0) { eventPublisher.publishEvent(any()) }
        }

        @Test
        fun `이미 FAILED 상태이면 이벤트를 발행하지 않는다`() {
            val timestamp = currentTimestamp()
            val diagnosis = createDiagnosis(DiagnosisStatus.FAILED)
            val signature = generateSignature(callbackSecret, timestamp, rawBody)
            val payload =
                SurveyReportCallbackPayload(
                    event = "survey_report.failed",
                    requestId = requestId,
                    sentAt = "2026-04-03T10:00:00Z",
                )
            every { diagnosisAdaptor.findByRequestId(requestId) } returns diagnosis
            every { objectMapper.readValue(rawBody, SurveyReportCallbackPayload::class.java) } returns payload

            useCase.execute(signature, timestamp, "survey_report.failed", rawBody)

            verify(exactly = 0) { eventPublisher.publishEvent(any()) }
        }
    }

    @Nested
    inner class EventPublishing {
        @Test
        fun `survey_report_completed 이벤트 수신 시 SurveyReportCompletedEvent가 발행된다`() {
            val timestamp = currentTimestamp()
            val diagnosis = createDiagnosis()
            val signature = generateSignature(callbackSecret, timestamp, rawBody)
            val result = mapOf<String, Any>("score" to 95)
            val payload =
                SurveyReportCallbackPayload(
                    event = "survey_report.completed",
                    requestId = requestId,
                    sentAt = "2026-04-03T10:00:00Z",
                    result = result,
                )
            every { diagnosisAdaptor.findByRequestId(requestId) } returns diagnosis
            every { objectMapper.readValue(rawBody, SurveyReportCallbackPayload::class.java) } returns payload
            every { objectMapper.writeValueAsString(result) } returns "{\"score\":95}"

            useCase.execute(signature, timestamp, "survey_report.completed", rawBody)

            val eventSlot = slot<SurveyReportCompletedEvent>()
            verify { eventPublisher.publishEvent(capture(eventSlot)) }
            assertAll(
                { assertEquals(diagnosis.id, eventSlot.captured.diagnosisId) },
                { assertEquals("홍길동", eventSlot.captured.studentName) },
                { assertEquals("010-1234-5678", eventSlot.captured.studentPhone) },
                { assertEquals("010-9876-5432", eventSlot.captured.parentPhone) },
                { assertEquals("{\"score\":95}", eventSlot.captured.reportData) },
            )
        }

        @Test
        fun `survey_report_failed 이벤트 수신 시 SurveyReportFailedEvent가 발행된다`() {
            val timestamp = currentTimestamp()
            val diagnosis = createDiagnosis()
            val signature = generateSignature(callbackSecret, timestamp, rawBody)
            val payload =
                SurveyReportCallbackPayload(
                    event = "survey_report.failed",
                    requestId = requestId,
                    sentAt = "2026-04-03T10:00:00Z",
                    error =
                        SurveyReportCallbackPayload.ErrorDetail(
                            code = "PROCESSING_ERROR",
                            message = "처리 중 오류 발생",
                            retryable = true,
                        ),
                )
            every { diagnosisAdaptor.findByRequestId(requestId) } returns diagnosis
            every { objectMapper.readValue(rawBody, SurveyReportCallbackPayload::class.java) } returns payload

            useCase.execute(signature, timestamp, "survey_report.failed", rawBody)

            val eventSlot = slot<SurveyReportFailedEvent>()
            verify { eventPublisher.publishEvent(capture(eventSlot)) }
            assertAll(
                { assertEquals(diagnosis.id, eventSlot.captured.diagnosisId) },
                { assertEquals("PROCESSING_ERROR", eventSlot.captured.errorCode) },
                { assertEquals(true, eventSlot.captured.retryable) },
            )
        }
    }
}
