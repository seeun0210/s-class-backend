package com.sclass.backoffice.webhook.usecase

import com.sclass.domain.domains.webhook.adaptor.WebhookAdaptor
import com.sclass.domain.domains.webhook.domain.Webhook
import com.sclass.domain.domains.webhook.domain.WebhookFieldMapping
import com.sclass.domain.domains.webhook.domain.WebhookType
import com.sclass.domain.domains.webhook.exception.WebhookNotFoundException
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DeleteWebhookUseCaseTest {
    private val webhookAdaptor = mockk<WebhookAdaptor>()
    private val useCase = DeleteWebhookUseCase(webhookAdaptor)

    @Nested
    inner class Execute {
        @Test
        fun `존재하는 웹훅을 삭제한다`() {
            val webhook =
                Webhook.create(
                    name = "테스트 웹훅",
                    type = WebhookType.GOOGLE_FORM_DIAGNOSIS,
                    fieldMapping = WebhookFieldMapping("학생 이름", "학생 연락처", null),
                )
            every { webhookAdaptor.findById(webhook.id) } returns webhook
            every { webhookAdaptor.delete(webhook.id) } just runs

            useCase.execute(webhook.id)

            verify { webhookAdaptor.delete(webhook.id) }
        }

        @Test
        fun `존재하지 않는 웹훅 삭제 시 WebhookNotFoundException을 던진다`() {
            every { webhookAdaptor.findById(any()) } throws WebhookNotFoundException()

            assertThrows(WebhookNotFoundException::class.java) {
                useCase.execute("non-existent-id")
            }
        }
    }
}
