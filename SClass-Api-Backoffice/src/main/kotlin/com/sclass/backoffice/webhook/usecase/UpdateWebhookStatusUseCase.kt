package com.sclass.backoffice.webhook.usecase

import com.sclass.backoffice.webhook.dto.UpdateWebhookStatusRequest
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.webhook.adaptor.WebhookAdaptor
import org.springframework.transaction.annotation.Transactional

@UseCase
class UpdateWebhookStatusUseCase(
    private val webhookAdaptor: WebhookAdaptor,
) {
    @Transactional
    fun execute(
        id: String,
        request: UpdateWebhookStatusRequest,
    ) {
        val webhook = webhookAdaptor.findById(id)
        if (webhook.status != request.status) {
            webhook.toggleStatus()
        }
    }
}
