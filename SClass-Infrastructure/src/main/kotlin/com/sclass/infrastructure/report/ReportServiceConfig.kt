package com.sclass.infrastructure.report

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient

@Configuration
@ConditionalOnProperty(prefix = "report-service", name = ["enabled"], havingValue = "true")
@EnableConfigurationProperties(ReportServiceProperties::class)
class ReportServiceConfig {
    @Bean("reportServiceWebClient")
    fun reportServiceWebClient(properties: ReportServiceProperties): WebClient =
        WebClient
            .builder()
            .baseUrl(properties.baseUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build()
}
