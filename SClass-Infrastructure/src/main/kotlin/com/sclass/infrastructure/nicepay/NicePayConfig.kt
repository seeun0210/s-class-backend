package com.sclass.infrastructure.nicepay

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration
import java.util.concurrent.TimeUnit

@Configuration
@EnableConfigurationProperties(NicePayProperties::class)
class NicePayConfig {
    @Bean
    fun nicePayWebClient(properties: NicePayProperties): WebClient {
        val httpClient =
            HttpClient
                .create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, properties.connectTimeoutMs)
                .responseTimeout(Duration.ofSeconds(properties.readTimeoutSeconds))
                .doOnConnected { conn ->
                    conn.addHandlerLast(ReadTimeoutHandler(properties.readTimeoutSeconds, TimeUnit.SECONDS))
                    conn.addHandlerLast(WriteTimeoutHandler(properties.readTimeoutSeconds, TimeUnit.SECONDS))
                }

        return WebClient
            .builder()
            .baseUrl(properties.baseUrl)
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .build()
    }
}
