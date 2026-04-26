package com.sclass.infrastructure.oauth.config

data class ProviderConfig(
    val clientId: String = "",
    val clientSecret: String = "",
    val appId: Long = 0,
)
