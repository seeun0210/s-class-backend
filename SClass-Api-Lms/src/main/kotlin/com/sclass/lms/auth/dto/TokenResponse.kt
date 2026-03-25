package com.sclass.lms.auth.dto

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
)
