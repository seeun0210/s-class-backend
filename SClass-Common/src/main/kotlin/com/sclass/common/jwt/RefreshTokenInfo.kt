package com.sclass.common.jwt

data class RefreshTokenInfo(
    val userId: String,
    val tokenId: String,
    val role: String,
)
