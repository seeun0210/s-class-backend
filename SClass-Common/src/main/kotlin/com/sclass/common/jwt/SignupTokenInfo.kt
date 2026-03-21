package com.sclass.common.jwt

data class SignupTokenInfo(
    val oauthId: String,
    val provider: String,
    val email: String,
    val name: String,
    val role: String,
    val platform: String,
)
