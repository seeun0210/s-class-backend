package com.sclass.lms.auth.dto

import jakarta.validation.constraints.NotBlank

data class SendPhoneCodeRequest(
    @field:NotBlank
    val phoneNumber: String,
)
