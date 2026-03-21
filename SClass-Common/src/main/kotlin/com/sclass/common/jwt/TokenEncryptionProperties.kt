package com.sclass.common.jwt

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "auth.token-encryption")
data class TokenEncryptionProperties(
    val secretKey: String,
)
