package com.sclass.infrastructure.s3

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "cloud.aws.s3")
data class S3Properties(
    val bucket: String,
    val region: String,
    val accessKey: String,
    val secretKey: String,
    val endpoint: String? = null,
)
