package com.sclass.backoffice.webhook.usecase

import com.sclass.backoffice.webhook.dto.WebhookLogResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.webhook.adaptor.WebhookLogAdaptor
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetWebhookLogListUseCase(
    private val webhookLogAdaptor: WebhookLogAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(
        webhookId: String,
        pageable: Pageable,
    ): Page<WebhookLogResponse> =
        webhookLogAdaptor.findAllByWebhookId(webhookId, pageable).map {
            WebhookLogResponse.from(it)
        }
}
