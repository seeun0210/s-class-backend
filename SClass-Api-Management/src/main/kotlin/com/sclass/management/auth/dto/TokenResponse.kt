package com.sclass.management.auth.dto

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
)
