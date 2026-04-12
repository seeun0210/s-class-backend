package com.sclass.infrastructure.report

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "report-service")
data class ReportServiceProperties(
    val baseUrl: String,
    val timeoutSeconds: Long = 30,
    val callbackSecret: String = "",
    val callbackBaseUrl: String = "",
)
