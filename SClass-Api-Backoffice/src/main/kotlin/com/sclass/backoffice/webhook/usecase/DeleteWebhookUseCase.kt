package com.sclass.backoffice.webhook.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.webhook.adaptor.WebhookAdaptor
import org.springframework.transaction.annotation.Transactional

@UseCase
class DeleteWebhookUseCase(
    private val webhookAdaptor: WebhookAdaptor,
) {
    @Transactional
    fun execute(id: String) {
        webhookAdaptor.findById(id)
        webhookAdaptor.delete(id)
    }
}
