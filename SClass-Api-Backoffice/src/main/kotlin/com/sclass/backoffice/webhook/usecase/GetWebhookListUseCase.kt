package com.sclass.backoffice.webhook.usecase

import com.sclass.backoffice.webhook.dto.WebhookResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.webhook.adaptor.WebhookAdaptor
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetWebhookListUseCase(
    private val webhookAdaptor: WebhookAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(): List<WebhookResponse> = webhookAdaptor.findAll().map { WebhookResponse.from(it) }
}
