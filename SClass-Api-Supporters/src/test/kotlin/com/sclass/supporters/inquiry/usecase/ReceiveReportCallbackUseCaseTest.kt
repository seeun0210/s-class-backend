package com.sclass.supporters.inquiry.usecase

import com.sclass.domain.domains.inquiryplan.adaptor.InquiryPlanAdaptor
import com.sclass.domain.domains.inquiryplan.domain.InquiryPlan
import com.sclass.domain.domains.inquiryplan.domain.InquiryPlanSourceType
import com.sclass.domain.domains.inquiryplan.domain.InquiryPlanStatus
import com.sclass.domain.domains.webhook.exception.WebhookInvalidSecretException
import com.sclass.infrastructure.report.ReportServiceProperties
import com.sclass.infrastructure.report.dto.ReportCallbackPayload
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import tools.jackson.databind.ObjectMapper
import java.time.Instant
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class ReceiveReportCallbackUseCaseTest {
    private lateinit var inquiryPlanAdaptor: InquiryPlanAdaptor
    private lateinit var objectMapper: ObjectMapper
    private lateinit var properties: ReportServiceProperties
    private lateinit var useCase: ReceiveReportCallbackUseCase

    private val secret = "test-secret-key"

    @BeforeEach
    fun setUp() {
        inquiryPlanAdaptor = mockk()
        objectMapper = mockk()
        properties =
            ReportServiceProperties(
                baseUrl = "https://report.example.com",
                callbackSecret = secret,
                callbackBaseUrl = "https://supporters.example.com",
            )
        useCase = ReceiveReportCallbackUseCase(inquiryPlanAdaptor, objectMapper, properties)
    }

    private fun pendingPlan() =
        InquiryPlan(
            id = 1L,
            sourceType = InquiryPlanSourceType.LESSON,
            sourceRefId = 10L,
            requestedByUserId = "user-id-00000000001",
            status = InquiryPlanStatus.PENDING,
        )

    private fun sign(
        timestamp: String,
        body: String,
    ): String =
        Mac
            .getInstance("HmacSHA256")
            .apply { init(SecretKeySpec(secret.toByteArray(), "HmacSHA256")) }
            .doFinal("$timestamp.$body".toByteArray())
            .joinToString("", prefix = "sha256=") { "%02x".format(it) }

    private fun completedPayload() =
        ReportCallbackPayload(
            event = "report.completed",
            requestId = "1",
            sentAt = "2026-04-11T00:00:00Z",
            result = ReportCallbackPayload.ReportResult(jobId = "job-abc", topic = "제로음료와 혈당"),
        )

    private fun failedPayload() =
        ReportCallbackPayload(
            event = "report.failed",
            requestId = "1",
            sentAt = "2026-04-11T00:00:00Z",
            error = ReportCallbackPayload.ErrorDetail(code = "E001", message = "생성 실패", retryable = false),
        )

    @Test
    fun `report_completed 이벤트 수신 시 plan이 READY로 전환된다`() {
        val planSlot = slot<InquiryPlan>()
        val rawBody = """{"event":"report.completed","requestId":"1","sentAt":"2026-04-11T00:00:00Z"}"""
        every { objectMapper.readValue(rawBody, ReportCallbackPayload::class.java) } returns completedPayload()
        every { inquiryPlanAdaptor.findById(1L) } returns pendingPlan()
        every { inquiryPlanAdaptor.save(capture(planSlot)) } answers { planSlot.captured }

        val timestamp = Instant.now().epochSecond.toString()
        useCase.execute(sign(timestamp, rawBody), timestamp, rawBody)

        assertEquals(InquiryPlanStatus.READY, planSlot.captured.status)
        assertEquals("제로음료와 혈당", planSlot.captured.topic)
    }

    @Test
    fun `report_failed 이벤트 수신 시 plan이 FAILED로 전환된다`() {
        val planSlot = slot<InquiryPlan>()
        val rawBody = """{"event":"report.failed","requestId":"1","sentAt":"2026-04-11T00:00:00Z"}"""
        every { objectMapper.readValue(rawBody, ReportCallbackPayload::class.java) } returns failedPayload()
        every { inquiryPlanAdaptor.findById(1L) } returns pendingPlan()
        every { inquiryPlanAdaptor.save(capture(planSlot)) } answers { planSlot.captured }

        val timestamp = Instant.now().epochSecond.toString()
        useCase.execute(sign(timestamp, rawBody), timestamp, rawBody)

        assertEquals(InquiryPlanStatus.FAILED, planSlot.captured.status)
        assertEquals("생성 실패", planSlot.captured.failureReason)
    }

    @Test
    fun `서명이 틀리면 WebhookInvalidSecretException이 발생한다`() {
        val rawBody = """{"event":"report.completed","requestId":"1","sentAt":"2026-04-11T00:00:00Z"}"""
        val timestamp = Instant.now().epochSecond.toString()

        assertThrows<WebhookInvalidSecretException> {
            useCase.execute("sha256=invalidsignature", timestamp, rawBody)
        }
    }

    @Test
    fun `타임스탬프가 5분을 초과하면 WebhookInvalidSecretException이 발생한다`() {
        val rawBody = """{"event":"report.completed","requestId":"1","sentAt":"2026-04-11T00:00:00Z"}"""
        val oldTimestamp = (Instant.now().epochSecond - 400).toString()

        assertThrows<WebhookInvalidSecretException> {
            useCase.execute(sign(oldTimestamp, rawBody), oldTimestamp, rawBody)
        }
    }

    @Test
    fun `plan이 이미 READY 상태이면 아무 처리도 하지 않는다`() {
        val readyPlan =
            InquiryPlan(
                id = 1L,
                sourceType = InquiryPlanSourceType.LESSON,
                sourceRefId = 10L,
                requestedByUserId = "user-id-00000000001",
                status = InquiryPlanStatus.READY,
                topic = "기존 토픽",
            )
        val rawBody = """{"event":"report.completed","requestId":"1","sentAt":"2026-04-11T00:00:00Z"}"""
        every { objectMapper.readValue(rawBody, ReportCallbackPayload::class.java) } returns completedPayload()
        every { inquiryPlanAdaptor.findById(1L) } returns readyPlan

        val timestamp = Instant.now().epochSecond.toString()
        useCase.execute(sign(timestamp, rawBody), timestamp, rawBody)

        verify(exactly = 0) { inquiryPlanAdaptor.save(any()) }
    }
}
