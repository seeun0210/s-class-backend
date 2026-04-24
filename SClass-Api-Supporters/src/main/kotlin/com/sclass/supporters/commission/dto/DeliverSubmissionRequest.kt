package com.sclass.supporters.commission.dto

import jakarta.validation.constraints.NotEmpty

data class DeliverSubmissionRequest(
    @field:NotEmpty
    val fileIds: List<String>,
)
