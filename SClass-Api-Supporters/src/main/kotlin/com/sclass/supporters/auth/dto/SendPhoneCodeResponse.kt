package com.sclass.supporters.auth.dto

data class SendPhoneCodeResponse(
    val expiresInSeconds: Long = 300,
)
