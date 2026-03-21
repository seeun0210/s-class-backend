package com.sclass.infrastructure.oauth.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(OAuthProperties::class)
class OAuthConfig
