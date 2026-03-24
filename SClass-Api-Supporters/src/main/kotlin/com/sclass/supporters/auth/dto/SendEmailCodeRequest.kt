package com.sclass.supporters.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class SendEmailCodeRequest(
    @field:NotBlank
    @field:Email
    val email: String,
)
