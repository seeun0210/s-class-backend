package com.sclass.domain.domains.webhook.repository

import com.sclass.domain.domains.webhook.domain.Webhook
import org.springframework.data.jpa.repository.JpaRepository

interface WebhookRepository : JpaRepository<Webhook, String> {
    fun findBySecret(secret: String): Webhook?
}
