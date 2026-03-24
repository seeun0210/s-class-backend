package com.sclass.infrastructure.email

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "email.smtp")
data class EmailProperties(
    val enabled: Boolean = false,
)
