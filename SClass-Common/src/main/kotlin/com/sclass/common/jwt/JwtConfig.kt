package com.sclass.common.jwt

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(JwtProperties::class, TokenEncryptionProperties::class)
class JwtConfig
