package com.sclass.infrastructure.nicepay

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
@EnableConfigurationProperties(NicePayProperties::class)
class NicePayConfig {
    @Bean
    fun nicePayWebClient(properties: NicePayProperties): WebClient =
        WebClient
            .builder()
            .baseUrl(properties.baseUrl)
            .build()
}
