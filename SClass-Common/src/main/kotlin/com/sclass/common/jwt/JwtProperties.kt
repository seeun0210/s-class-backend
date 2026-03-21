package com.sclass.common.jwt

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "auth.jwt")
data class JwtProperties(
    val secretKey: String,
    val accessExp: Long,
    val refreshExp: Long,
)
