package com.sclass.supporters.commission.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class ProposeTopicsRequest(
    @field:Valid
    @field:NotEmpty
    val topics: List<TopicRequest>,
)

data class TopicRequest(
    @field:NotBlank
    val topicId: String,

    @field:NotBlank
    val title: String,

    val description: String? = null,
)
