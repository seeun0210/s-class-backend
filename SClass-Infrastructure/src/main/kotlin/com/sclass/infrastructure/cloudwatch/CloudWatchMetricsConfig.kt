package com.sclass.infrastructure.cloudwatch

import io.micrometer.cloudwatch2.CloudWatchConfig
import io.micrometer.cloudwatch2.CloudWatchMeterRegistry
import io.micrometer.core.instrument.Clock
import io.micrometer.core.instrument.config.MeterFilter
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient
import java.time.Duration

@Configuration
@ConditionalOnProperty(
    name = ["management.cloudwatch.metrics.enabled"],
    havingValue = "true",
)
@EnableConfigurationProperties(CloudWatchMetricsProperties::class)
class CloudWatchMetricsConfig(
    private val properties: CloudWatchMetricsProperties,
) {
    @Bean
    fun cloudWatchAsyncClient(): CloudWatchAsyncClient =
        CloudWatchAsyncClient
            .builder()
            .region(Region.of(properties.region))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build()

    @Bean
    fun cloudWatchMeterRegistry(cloudWatchAsyncClient: CloudWatchAsyncClient): CloudWatchMeterRegistry {
        val config =
            object : CloudWatchConfig {
                override fun get(key: String): String? = null

                override fun namespace(): String = properties.namespace

                override fun step(): Duration = Duration.ofSeconds(properties.step)
            }

        val registry = CloudWatchMeterRegistry(config, Clock.SYSTEM, cloudWatchAsyncClient)

        registry.config().meterFilter(
            MeterFilter.accept { id ->
                ALLOWED_PREFIXES.any { prefix -> id.name.startsWith(prefix) }
            },
        )
        registry.config().meterFilter(MeterFilter.deny())

        return registry
    }

    companion object {
        private val ALLOWED_PREFIXES =
            listOf(
                "jvm.memory",
                "jvm.gc",
                "process.cpu",
                "system.cpu",
                "hikaricp.",
                "http.server.requests",
            )
    }
}
