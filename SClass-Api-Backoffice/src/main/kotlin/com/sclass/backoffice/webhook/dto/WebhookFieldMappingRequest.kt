package com.sclass.backoffice.webhook.dto

import jakarta.validation.constraints.NotBlank

data class WebhookFieldMappingRequest(
    @field:NotBlank
    val studentNameQuestion: String,

    @field:NotBlank
    val studentPhoneQuestion: String,

    val parentPhoneQuestion: String?,
)
