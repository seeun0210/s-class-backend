package com.sclass.infrastructure.oauth.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration

@Configuration
@EnableConfigurationProperties(OAuthProperties::class)
class OAuthConfig {
    @Bean
    @Qualifier("oAuthWebClient")
    fun oAuthWebClient(): WebClient {
        val httpClient =
            HttpClient
                .create()
                .responseTimeout(Duration.ofSeconds(10))

        return WebClient
            .builder()
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .build()
    }
}
