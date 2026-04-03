package com.sclass.backoffice.webhook.usecase

import com.sclass.backoffice.webhook.dto.CreateWebhookRequest
import com.sclass.backoffice.webhook.dto.WebhookFieldMappingRequest
import com.sclass.domain.domains.webhook.adaptor.WebhookAdaptor
import com.sclass.domain.domains.webhook.domain.Webhook
import com.sclass.domain.domains.webhook.domain.WebhookType
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

class CreateWebhookUseCaseTest {
    private val webhookAdaptor = mockk<WebhookAdaptor>()
    private val useCase = CreateWebhookUseCase(webhookAdaptor)

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

    private fun createRequest() =
        CreateWebhookRequest(
            name = "2026 진단 폼",
            type = WebhookType.GOOGLE_FORM_DIAGNOSIS,
            fieldMapping =
                WebhookFieldMappingRequest(
                    studentNameQuestion = "학생 이름",
                    studentPhoneQuestion = "학생 연락처",
                    parentPhoneQuestion = "학부모 연락처",
                ),
        )

    @Test
    fun `웹훅을 생성하고 secret과 scriptCode를 반환한다`() {
        val slot = slot<Webhook>()
        every { webhookAdaptor.save(capture(slot)) } answers { slot.captured }

        val result = useCase.execute(createRequest())

        assertAll(
            { assertEquals("2026 진단 폼", result.name) },
            { assertEquals(WebhookType.GOOGLE_FORM_DIAGNOSIS, result.type) },
            { assertEquals(64, result.secret.length) },
            { assertTrue(result.scriptCode.contains(result.id)) },
            { assertTrue(result.scriptCode.contains(result.secret)) },
            { assertTrue(result.scriptCode.contains("onFormSubmit")) },
        )
    }

    @Test
    fun `생성된 scriptCode에 backoffice 서버 URL이 포함된다`() {
        val slot = slot<Webhook>()
        every { webhookAdaptor.save(capture(slot)) } answers { slot.captured }

        val result = useCase.execute(createRequest())

        assertTrue(result.scriptCode.contains("backoffice.example.com"))
    }

    @Test
    fun `fieldMapping이 요청값 그대로 저장된다`() {
        val slot = slot<Webhook>()
        every { webhookAdaptor.save(capture(slot)) } answers { slot.captured }

        useCase.execute(createRequest())

        assertAll(
            { assertEquals("학생 이름", slot.captured.fieldMapping.studentNameQuestion) },
            { assertEquals("학생 연락처", slot.captured.fieldMapping.studentPhoneQuestion) },
            { assertEquals("학부모 연락처", slot.captured.fieldMapping.parentPhoneQuestion) },
        )
    }
}
