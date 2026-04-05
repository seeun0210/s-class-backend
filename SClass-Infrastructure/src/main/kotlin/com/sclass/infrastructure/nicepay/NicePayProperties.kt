package com.sclass.infrastructure.nicepay

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "nicepay")
data class NicePayProperties(
    val clientKey: String,
    val secretKey: String,
    val baseUrl: String = "https://api.nicepay.co.kr",
)
