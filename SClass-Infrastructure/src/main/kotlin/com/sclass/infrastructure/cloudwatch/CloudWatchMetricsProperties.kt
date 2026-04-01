package com.sclass.infrastructure.cloudwatch

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "management.cloudwatch.metrics")
data class CloudWatchMetricsProperties(
    val enabled: Boolean = false,
    val namespace: String = "SClass",
    val step: Long = 60,
    val region: String = "ap-northeast-1",
)
