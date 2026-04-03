package com.sclass.domain.domains.webhook.adaptor

import com.sclass.domain.common.vo.Ulid
import com.sclass.domain.domains.webhook.domain.Webhook
import com.sclass.domain.domains.webhook.domain.WebhookFieldMapping
import com.sclass.domain.domains.webhook.domain.WebhookType
import com.sclass.domain.domains.webhook.exception.WebhookNotFoundException
import com.sclass.domain.domains.webhook.repository.WebhookRepository
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.Optional

class WebhookAdaptorTest {
    private val webhookRepository = mockk<WebhookRepository>()
    private val adaptor = WebhookAdaptor(webhookRepository)

    private fun createWebhook() =
        Webhook.create(
            name = "테스트 웹훅",
            type = WebhookType.GOOGLE_FORM_DIAGNOSIS,
            fieldMapping = WebhookFieldMapping("학생 이름", "학생 연락처", null),
        )

    @Nested
    inner class FindById {
        @Test
        fun `존재하는 id로 조회하면 webhook을 반환한다`() {
            val webhook = createWebhook()
            every { webhookRepository.findById(webhook.id) } returns Optional.of(webhook)

            val result = adaptor.findById(webhook.id)

            assertEquals(webhook, result)
        }

        @Test
        fun `존재하지 않는 id로 조회하면 WebhookNotFoundException을 던진다`() {
            every { webhookRepository.findById(any()) } returns Optional.empty()

            assertThrows(WebhookNotFoundException::class.java) {
                adaptor.findById(Ulid.generate())
            }
        }
    }

    @Nested
    inner class Save {
        @Test
        fun `webhook을 저장하고 반환한다`() {
            val webhook = createWebhook()
            every { webhookRepository.save(webhook) } returns webhook

            val result = adaptor.save(webhook)

            verify { webhookRepository.save(webhook) }
            assertEquals(webhook, result)
        }
    }

    @Nested
    inner class Delete {
        @Test
        fun `id로 webhook을 삭제한다`() {
            val id = Ulid.generate()
            every { webhookRepository.deleteById(id) } just runs

            adaptor.delete(id)

            verify { webhookRepository.deleteById(id) }
        }
    }
}
