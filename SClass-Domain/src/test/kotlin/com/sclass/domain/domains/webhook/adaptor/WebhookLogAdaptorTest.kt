package com.sclass.domain.domains.webhook.adaptor

import com.sclass.domain.common.vo.Ulid
import com.sclass.domain.domains.webhook.domain.WebhookLog
import com.sclass.domain.domains.webhook.exception.WebhookLogNotFoundException
import com.sclass.domain.domains.webhook.repository.WebhookLogRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.Optional

class WebhookLogAdaptorTest {
    private val webhookLogRepository = mockk<WebhookLogRepository>()
    private val adaptor = WebhookLogAdaptor(webhookLogRepository)

    private fun createLog(webhookId: String = Ulid.generate()) =
        WebhookLog(
            webhookId = webhookId,
            payload = "{}",
        )

    @Nested
    inner class FindById {
        @Test
        fun `존재하는 id로 조회하면 webhookLog를 반환한다`() {
            val log = createLog()
            every { webhookLogRepository.findById(log.id) } returns Optional.of(log)

            val result = adaptor.findById(log.id)

            assertEquals(log, result)
        }

        @Test
        fun `존재하지 않는 id로 조회하면 WebhookLogNotFoundException을 던진다`() {
            every { webhookLogRepository.findById(any()) } returns Optional.empty()

            assertThrows(WebhookLogNotFoundException::class.java) {
                adaptor.findById(Ulid.generate())
            }
        }
    }

    @Nested
    inner class Save {
        @Test
        fun `webhookLog를 저장하고 반환한다`() {
            val log = createLog()
            every { webhookLogRepository.save(log) } returns log

            val result = adaptor.save(log)

            verify { webhookLogRepository.save(log) }
            assertEquals(log, result)
        }
    }
}
