package com.sclass.backoffice.webhook.dto

import com.sclass.domain.domains.webhook.domain.WebhookFieldMapping

data class WebhookFieldMappingResponse(
    val studentNameQuestion: String,
    val studentPhoneQuestion: String,
    val parentPhoneQuestion: String?,
) {
    companion object {
        fun from(mapping: WebhookFieldMapping): WebhookFieldMappingResponse =
            WebhookFieldMappingResponse(
                studentNameQuestion = mapping.studentNameQuestion,
                studentPhoneQuestion = mapping.studentPhoneQuestion,
                parentPhoneQuestion = mapping.parentPhoneQuestion,
            )
    }
}
