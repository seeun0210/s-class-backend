package com.sclass.domain.domains.webhook.domain

import com.sclass.domain.common.model.BaseTimeEntity
import com.sclass.domain.common.vo.Ulid
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "webhook_logs")
class WebhookLog(
    @Id
    @Column(length = 26)
    val id: String = Ulid.generate(),

    @Column(length = 26, nullable = false)
    val webhookId: String,

    @Column(columnDefinition = "TEXT", nullable = false)
    val payload: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: WebhookLogStatus = WebhookLogStatus.RECEIVED,

    @Column(columnDefinition = "TEXT")
    var errorMessage: String? = null,
) : BaseTimeEntity() {
    fun markProcessing() {
        status = WebhookLogStatus.PROCESSING
    }

    fun markCompleted() {
        status = WebhookLogStatus.COMPLETED
    }

    fun markFailed(error: String) {
        status = WebhookLogStatus.FAILED
        errorMessage = error
    }
}
