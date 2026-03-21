package com.sclass.supporters.auth.dto

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
)
