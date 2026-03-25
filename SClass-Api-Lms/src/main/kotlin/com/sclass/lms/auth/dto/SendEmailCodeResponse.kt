package com.sclass.lms.auth.dto

data class SendEmailCodeResponse(
    val expiresInSeconds: Long = 300,
)
