package com.sclass.infrastructure.s3

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import java.net.URI

@Configuration
@ConditionalOnProperty("cloud.aws.s3.bucket")
@EnableConfigurationProperties(S3Properties::class)
class S3Config(
    private val s3Properties: S3Properties,
) {
    private fun credentialsProvider(): AwsCredentialsProvider =
        if (s3Properties.accessKey.isNotBlank()) {
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(s3Properties.accessKey, s3Properties.secretKey),
            )
        } else {
            DefaultCredentialsProvider.create()
        }

    @Bean
    fun s3Client(): S3Client {
        val builder =
            S3Client
                .builder()
                .region(Region.of(s3Properties.region))
                .credentialsProvider(credentialsProvider())

        s3Properties.endpoint?.takeIf { it.isNotBlank() }?.let {
            builder
                .endpointOverride(URI.create(it))
                .forcePathStyle(true)
        }

        return builder.build()
    }

    @Bean
    fun s3Presigner(): S3Presigner {
        val builder =
            S3Presigner
                .builder()
                .region(Region.of(s3Properties.region))
                .credentialsProvider(credentialsProvider())

        s3Properties.endpoint?.takeIf { it.isNotBlank() }?.let {
            builder
                .endpointOverride(URI.create(it))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
        }

        return builder.build()
    }
}
