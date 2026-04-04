package com.sclass.domain.domains.webhook.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.webhook.domain.WebhookLog
import com.sclass.domain.domains.webhook.exception.WebhookLogNotFoundException
import com.sclass.domain.domains.webhook.repository.WebhookLogRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

@Adaptor
class WebhookLogAdaptor(
    private val webhookLogRepository: WebhookLogRepository,
) {
    fun save(webhookLog: WebhookLog): WebhookLog = webhookLogRepository.save(webhookLog)

    fun findById(id: String): WebhookLog = webhookLogRepository.findById(id).orElseThrow { WebhookLogNotFoundException() }

    fun findAllByWebhookId(
        webhookId: String,
        pageable: Pageable,
    ): Page<WebhookLog> = webhookLogRepository.findAllByWebhookId(webhookId, pageable)

    fun findByDiagnosisIdOrNull(diagnosisId: String): WebhookLog? = webhookLogRepository.findByDiagnosisId(diagnosisId)
}
