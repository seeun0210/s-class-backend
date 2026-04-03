package com.sclass.backoffice.webhook.usecase

import com.sclass.domain.domains.webhook.adaptor.WebhookAdaptor
import com.sclass.domain.domains.webhook.domain.Webhook
import com.sclass.domain.domains.webhook.domain.WebhookFieldMapping
import com.sclass.domain.domains.webhook.domain.WebhookStatus
import com.sclass.domain.domains.webhook.domain.WebhookType
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetWebhookListUseCaseTest {
    private val webhookAdaptor = mockk<WebhookAdaptor>()
    private val useCase = GetWebhookListUseCase(webhookAdaptor)

    @Test
    fun `웹훅 목록을 반환한다`() {
        val webhooks =
            listOf(
                Webhook.create("폼A", WebhookType.GOOGLE_FORM_DIAGNOSIS, WebhookFieldMapping("이름", "연락처", null)),
                Webhook.create("폼B", WebhookType.GOOGLE_FORM_DIAGNOSIS, WebhookFieldMapping("이름", "연락처", null)),
            )
        every { webhookAdaptor.findAll() } returns webhooks

        val result = useCase.execute()

        assertAll(
            { assertEquals(2, result.size) },
            { assertEquals("폼A", result[0].name) },
            { assertEquals("폼B", result[1].name) },
        )
    }

    @Test
    fun `웹훅이 없으면 빈 목록을 반환한다`() {
        every { webhookAdaptor.findAll() } returns emptyList()

        val result = useCase.execute()

        assertEquals(0, result.size)
    }

    @Test
    fun `웹훅 status가 응답에 포함된다`() {
        val webhook = Webhook.create("폼A", WebhookType.GOOGLE_FORM_DIAGNOSIS, WebhookFieldMapping("이름", "연락처", null))
        every { webhookAdaptor.findAll() } returns listOf(webhook)

        val result = useCase.execute()

        assertEquals(WebhookStatus.ACTIVE, result[0].status)
    }
}
