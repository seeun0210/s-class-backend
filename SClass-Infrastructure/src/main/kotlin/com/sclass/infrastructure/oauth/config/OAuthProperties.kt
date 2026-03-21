package com.sclass.infrastructure.oauth.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "oauth2")
class OAuthProperties {
    val providers: MutableMap<String, ProviderConfig> = mutableMapOf()
}
