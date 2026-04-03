package com.sclass.backoffice.webhook.dto

import com.sclass.domain.domains.webhook.domain.WebhookLog
import com.sclass.domain.domains.webhook.domain.WebhookLogStatus
import java.time.LocalDateTime

data class WebhookLogResponse(
    val id: String,
    val status: WebhookLogStatus,
    val errorMessage: String?,
    val diagnosisId: String?,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(log: WebhookLog): WebhookLogResponse =
            WebhookLogResponse(
                id = log.id,
                status = log.status,
                errorMessage = log.errorMessage,
                diagnosisId = log.diagnosisId,
                createdAt = log.createdAt,
            )
    }
}
