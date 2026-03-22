package com.sclass.backoffice.auth.dto

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
)
