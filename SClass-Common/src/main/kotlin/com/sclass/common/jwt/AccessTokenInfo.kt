package com.sclass.common.jwt

data class AccessTokenInfo(
    val userId: String,
    val roles: List<String>,
)
