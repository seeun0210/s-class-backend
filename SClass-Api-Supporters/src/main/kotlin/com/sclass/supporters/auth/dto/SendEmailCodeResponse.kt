package com.sclass.supporters.auth.dto

data class SendEmailCodeResponse(
    val expiresInSeconds: Long = 300,
)
