package com.sclass.domain.domains.webhook.repository

import com.sclass.domain.domains.webhook.domain.WebhookLog
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface WebhookLogRepository : JpaRepository<WebhookLog, String> {
    fun findAllByWebhookId(
        webhookId: String,
        pageable: Pageable,
    ): Page<WebhookLog>

    fun findByDiagnosisId(diagnosisId: String): WebhookLog?
}
