package com.sclass.backoffice.teacher.dto

import com.sclass.domain.domains.user.domain.Platform
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateTeacherRequest(
    @field:NotBlank
    @field:Email
    val email: String,

    @field:NotBlank
    val name: String,

    @field:NotNull
    val platform: Platform,
)
