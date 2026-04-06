package com.sclass.infrastructure.redis

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.data.redis")
data class RedisProperties(
    val host: String = "localhost",
    val port: Int = 6379,
    val password: String? = null,
)
