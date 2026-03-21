package com.sclass.infrastructure.oauth.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
@EnableConfigurationProperties(OAuthProperties::class)
class OAuthConfig {
    @Bean
    fun oAuthRestTemplate(): RestTemplate = RestTemplate()
}
