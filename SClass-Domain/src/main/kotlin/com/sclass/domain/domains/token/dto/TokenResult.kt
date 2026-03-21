package com.sclass.domain.domains.token.dto

data class TokenResult(
    val accessToken: String,
    val refreshToken: String,
)
