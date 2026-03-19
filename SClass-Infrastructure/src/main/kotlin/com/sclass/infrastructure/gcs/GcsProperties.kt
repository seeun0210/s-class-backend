package com.sclass.infrastructure.gcs

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "cloud.gcp.storage")
data class GcsProperties(
    val bucket: String,
    val projectId: String,
    val credentialsLocation: String? = null,
)
