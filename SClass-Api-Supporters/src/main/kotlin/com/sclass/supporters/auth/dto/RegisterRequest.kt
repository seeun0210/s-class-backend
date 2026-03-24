package com.sclass.supporters.auth.dto

import com.sclass.domain.domains.user.domain.Role
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:Email
    @field:NotBlank
    val email: String,

    @field:NotBlank
    @field:Size(min = 8, max = 100)
    val password: String,

    @field:NotBlank
    val name: String,

    @field:NotBlank
    val phoneNumber: String,

    @field:NotNull
    val role: Role,

    @field:NotBlank
    val phoneVerificationToken: String,

    @field:NotBlank
    val emailVerificationToken: String,
)
