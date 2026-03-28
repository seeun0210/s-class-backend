package com.sclass.backoffice.teacherassignment.dto

import com.sclass.domain.domains.user.domain.Platform
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class UnassignTeacherRequest(
    @field:NotBlank
    val studentUserId: String,

    @field:NotNull
    val platform: Platform,

    val organizationId: Long? = null,
)
