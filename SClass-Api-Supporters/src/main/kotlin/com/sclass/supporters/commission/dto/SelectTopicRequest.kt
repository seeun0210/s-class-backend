package com.sclass.supporters.commission.dto

import jakarta.validation.constraints.NotNull

data class SelectTopicRequest(
    @field:NotNull
    val isSelected: Boolean,
)
