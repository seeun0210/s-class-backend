package com.sclass.backoffice.webhook.usecase

import com.sclass.backoffice.webhook.dto.GoogleFormWebhookPayload
import com.sclass.backoffice.webhook.event.DiagnosisRequestedEvent
import com.sclass.domain.domains.diagnosis.adaptor.DiagnosisAdaptor
import com.sclass.domain.domains.diagnosis.domain.Diagnosis
import com.sclass.domain.domains.webhook.adaptor.WebhookAdaptor
import com.sclass.domain.domains.webhook.adaptor.WebhookLogAdaptor
import com.sclass.domain.domains.webhook.domain.Webhook
import com.sclass.domain.domains.webhook.domain.WebhookFieldMapping
import com.sclass.domain.domains.webhook.domain.WebhookLog
import com.sclass.domain.domains.webhook.domain.WebhookStatus
import com.sclass.domain.domains.webhook.domain.WebhookType
import com.sclass.domain.domains.webhook.exception.WebhookInactiveException
import com.sclass.domain.domains.webhook.exception.WebhookInvalidSecretException
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import tools.jackson.databind.ObjectMapper

class ReceiveWebhookUseCaseTest {
    private val webhookAdaptor = mockk<WebhookAdaptor>()
    private val webhookLogAdaptor = mockk<WebhookLogAdaptor>()
    private val diagnosisAdaptor = mockk<DiagnosisAdaptor>()
    private val objectMapper = mockk<ObjectMapper>()
    private val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
    private val useCase =
        ReceiveWebhookUseCase(webhookAdaptor, webhookLogAdaptor, diagnosisAdaptor, objectMapper, eventPublisher)

    private val secret = "test-secret"
    private val activeWebhook =
        Webhook(
            name = "테스트 웹훅",
            type = WebhookType.GOOGLE_FORM_DIAGNOSIS,
            secret = secret,
            fieldMapping = WebhookFieldMapping("학생 이름", "학생 연락처", "학부모 연락처"),
        )
    private val payload =
        GoogleFormWebhookPayload(
            formId = "form-123",
            formTitle = "진단 폼",
            formResponseId = "response-1",
            submittedAt = "2026-04-03T10:00:00Z",
            answers =
                mapOf(
                    "학생 이름" to "홍길동",
                    "학생 연락처" to "010-1234-5678",
                    "학부모 연락처" to "010-9876-5432",
                ),
        )

    @BeforeEach
    fun setUpRequestContext() {
        val request = MockHttpServletRequest()
        request.scheme = "https"
        request.serverName = "backoffice.example.com"
        request.serverPort = 443
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request))
    }

    @AfterEach
    fun clearRequestContext() {
        RequestContextHolder.resetRequestAttributes()
    }

    @Nested
    inner class Execute {
        @Test
        fun `잘못된 secret이면 WebhookInvalidSecretException을 던진다`() {
            every { webhookAdaptor.findById(activeWebhook.id) } returns activeWebhook

            assertThrows(WebhookInvalidSecretException::class.java) {
                useCase.execute(activeWebhook.id, "wrong-secret", payload)
            }
        }

        @Test
        fun `INACTIVE 상태의 웹훅이면 WebhookInactiveException을 던진다`() {
            val inactiveWebhook =
                Webhook(
                    name = "비활성 웹훅",
                    type = WebhookType.GOOGLE_FORM_DIAGNOSIS,
                    secret = secret,
                    status = WebhookStatus.INACTIVE,
                    fieldMapping = WebhookFieldMapping("학생 이름", "학생 연락처", null),
                )
            every { webhookAdaptor.findById(inactiveWebhook.id) } returns inactiveWebhook

            assertThrows(WebhookInactiveException::class.java) {
                useCase.execute(inactiveWebhook.id, secret, payload)
            }
        }

        @Test
        fun `정상 요청 시 DiagnosisRequestedEvent가 발행된다`() {
            val savedDiagnosis =
                Diagnosis(
                    studentName = "홍길동",
                    studentPhone = "010-1234-5678",
                    parentPhone = "010-9876-5432",
                    requestData = "{}",
                )
            val savedLog = WebhookLog(webhookId = activeWebhook.id, payload = "{}")

            every { webhookAdaptor.findById(activeWebhook.id) } returns activeWebhook
            every { objectMapper.writeValueAsString(any()) } returns "{}"
            every { webhookLogAdaptor.save(any()) } returns savedLog
            every { diagnosisAdaptor.save(any()) } returns savedDiagnosis

            useCase.execute(activeWebhook.id, secret, payload)

            val eventSlot = slot<DiagnosisRequestedEvent>()
            verify { eventPublisher.publishEvent(capture(eventSlot)) }
            assertAll(
                { assertEquals(savedDiagnosis.id, eventSlot.captured.diagnosisId) },
                { assertEquals("홍길동", eventSlot.captured.studentName) },
                { assertEquals(payload.answers, eventSlot.captured.answers) },
                { assertEquals("https://backoffice.example.com/api/report-webhooks/survey-report", eventSlot.captured.callbackUrl) },
            )
        }

        @Test
        fun `정상 요청 시 WebhookLog가 진단 ID와 연결된다`() {
            val savedDiagnosis =
                Diagnosis(
                    studentName = "홍길동",
                    studentPhone = "010-1234-5678",
                    parentPhone = null,
                    requestData = "{}",
                )
            val logSlot = slot<WebhookLog>()

            every { webhookAdaptor.findById(activeWebhook.id) } returns activeWebhook
            every { objectMapper.writeValueAsString(any()) } returns "{}"
            every { webhookLogAdaptor.save(capture(logSlot)) } returns WebhookLog(webhookId = activeWebhook.id, payload = "{}")
            every { diagnosisAdaptor.save(any()) } returns savedDiagnosis

            useCase.execute(activeWebhook.id, secret, payload)

            assertEquals(activeWebhook.id, logSlot.captured.webhookId)
        }
    }
}
