package com.sclass.supporters.auth.dto

import jakarta.validation.constraints.NotBlank

data class SendPhoneCodeRequest(
    @field:NotBlank
    val phoneNumber: String,
)
