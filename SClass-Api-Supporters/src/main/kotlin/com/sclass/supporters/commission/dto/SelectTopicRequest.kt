package com.sclass.supporters.commission.dto

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotNull

data class SelectTopicRequest(
    @field:NotNull
    @JsonProperty("isSelected")
    val isSelected: Boolean,
)
