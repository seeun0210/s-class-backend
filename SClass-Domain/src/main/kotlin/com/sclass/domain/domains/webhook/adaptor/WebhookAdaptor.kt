package com.sclass.domain.domains.webhook.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.webhook.domain.Webhook
import com.sclass.domain.domains.webhook.exception.WebhookNotFoundException
import com.sclass.domain.domains.webhook.repository.WebhookRepository

@Adaptor
class WebhookAdaptor(
    private val webhookRepository: WebhookRepository,
) {
    fun save(webhook: Webhook): Webhook = webhookRepository.save(webhook)

    fun findById(id: String): Webhook = webhookRepository.findById(id).orElseThrow { WebhookNotFoundException() }

    fun findAll(): List<Webhook> = webhookRepository.findAll()

    fun delete(id: String) = webhookRepository.deleteById(id)
}
