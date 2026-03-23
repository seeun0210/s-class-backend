package com.sclass.supporters.auth.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class VerifyPhoneCodeRequest(
    @field:NotBlank
    val phoneNumber: String,

    @field:NotBlank
    @field:Size(min = 6, max = 6)
    val code: String,
)
