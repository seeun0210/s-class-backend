package com.sclass.lms.auth.dto

data class SendPhoneCodeResponse(
    val expiresInSeconds: Long = 300,
)
