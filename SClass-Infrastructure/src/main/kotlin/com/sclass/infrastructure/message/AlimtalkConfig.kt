package com.sclass.infrastructure.message

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
@ConditionalOnProperty(name = ["alimtalk.enabled"], havingValue = "true")
@EnableConfigurationProperties(AlimtalkProperties::class)
class AlimtalkConfig {
    @Bean
    @Qualifier("alimtalkWebClient")
    fun alimtalkWebClient(): WebClient =
        WebClient
            .builder()
            .baseUrl("https://sens.apigw.ntruss.com")
            .build()
}
