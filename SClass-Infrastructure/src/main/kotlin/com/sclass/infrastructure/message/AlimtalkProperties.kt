package com.sclass.infrastructure.message

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "alimtalk")
data class AlimtalkProperties(
    val enabled: Boolean = false,
    val serviceId: String = "",
    val accessKey: String = "",
    val secretKey: String = "",
    val plusFriendId: String = "",
)
