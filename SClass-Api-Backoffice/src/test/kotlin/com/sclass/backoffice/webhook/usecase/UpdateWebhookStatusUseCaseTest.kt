package com.sclass.backoffice.webhook.usecase

import com.sclass.backoffice.webhook.dto.UpdateWebhookStatusRequest
import com.sclass.domain.domains.webhook.adaptor.WebhookAdaptor
import com.sclass.domain.domains.webhook.domain.Webhook
import com.sclass.domain.domains.webhook.domain.WebhookFieldMapping
import com.sclass.domain.domains.webhook.domain.WebhookStatus
import com.sclass.domain.domains.webhook.domain.WebhookType
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UpdateWebhookStatusUseCaseTest {
    private val webhookAdaptor = mockk<WebhookAdaptor>()
    private val useCase = UpdateWebhookStatusUseCase(webhookAdaptor)

    private fun createWebhook() =
        Webhook.create(
            name = "테스트 웹훅",
            type = WebhookType.GOOGLE_FORM_DIAGNOSIS,
            fieldMapping = WebhookFieldMapping("학생 이름", "학생 연락처", null),
        )

    @Nested
    inner class Execute {
        @Test
        fun `ACTIVE 상태에서 INACTIVE 요청 시 status가 변경된다`() {
            val webhook = createWebhook()
            every { webhookAdaptor.findById(webhook.id) } returns webhook

            useCase.execute(webhook.id, UpdateWebhookStatusRequest(WebhookStatus.INACTIVE))

            assertEquals(WebhookStatus.INACTIVE, webhook.status)
        }

        @Test
        fun `이미 같은 status 요청 시 변경되지 않는다`() {
            val webhook = createWebhook()
            every { webhookAdaptor.findById(webhook.id) } returns webhook

            useCase.execute(webhook.id, UpdateWebhookStatusRequest(WebhookStatus.ACTIVE))

            assertEquals(WebhookStatus.ACTIVE, webhook.status)
        }
    }
}
