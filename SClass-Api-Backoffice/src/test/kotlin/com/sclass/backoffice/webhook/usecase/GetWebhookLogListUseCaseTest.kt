package com.sclass.backoffice.webhook.usecase

import com.sclass.domain.domains.webhook.adaptor.WebhookLogAdaptor
import com.sclass.domain.domains.webhook.domain.WebhookLog
import com.sclass.domain.domains.webhook.domain.WebhookLogStatus
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

class GetWebhookLogListUseCaseTest {
    private val webhookLogAdaptor = mockk<WebhookLogAdaptor>()
    private val useCase = GetWebhookLogListUseCase(webhookLogAdaptor)

    @Test
    fun `웹훅 로그 목록을 페이지네이션으로 반환한다`() {
        val webhookId = "webhook-id-123"
        val pageable = PageRequest.of(0, 20)
        val logs =
            listOf(
                WebhookLog(webhookId = webhookId, payload = "{}", status = WebhookLogStatus.COMPLETED),
                WebhookLog(webhookId = webhookId, payload = "{}", status = WebhookLogStatus.FAILED),
            )
        every { webhookLogAdaptor.findAllByWebhookId(webhookId, pageable) } returns PageImpl(logs, pageable, 2)

        val result = useCase.execute(webhookId, pageable)

        assertAll(
            { assertEquals(2, result.totalElements) },
            { assertEquals(WebhookLogStatus.COMPLETED, result.content[0].status) },
            { assertEquals(WebhookLogStatus.FAILED, result.content[1].status) },
        )
    }

    @Test
    fun `로그가 없으면 빈 페이지를 반환한다`() {
        val webhookId = "webhook-id-123"
        val pageable = PageRequest.of(0, 20)
        every { webhookLogAdaptor.findAllByWebhookId(webhookId, pageable) } returns PageImpl(emptyList(), pageable, 0)

        val result = useCase.execute(webhookId, pageable)

        assertEquals(0, result.totalElements)
    }
}
