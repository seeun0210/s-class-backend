package com.sclass.backoffice.teacherassignment.dto

import com.sclass.domain.domains.user.domain.Platform
import jakarta.validation.constraints.NotBlank
import software.amazon.awssdk.annotations.NotNull

data class UnassignTeacherRequest(
    @field:NotBlank
    val studentId: String,

    @field:NotNull
    val platform: Platform,

    val organizationId: Long? = null,
)
