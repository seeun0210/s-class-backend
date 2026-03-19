package com.sclass.infrastructure.gcs

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import java.io.FileInputStream

@Configuration
@EnableConfigurationProperties(GcsProperties::class)
class GcsConfig(
    private val gcsProperties: GcsProperties,
) {
    @Bean
    fun storage(): Storage {
        val credentials = loadCredentials()

        return StorageOptions
            .newBuilder()
            .setProjectId(gcsProperties.projectId)
            .setCredentials(credentials)
            .build()
            .service
    }

    private fun loadCredentials(): GoogleCredentials {
        val location = gcsProperties.credentialsLocation

        if (location.isNullOrBlank()) {
            return GoogleCredentials.getApplicationDefault()
        }

        return when {
            location.startsWith("classpath:") -> {
                val resource = ClassPathResource(location.substring(10))
                resource.inputStream.use { GoogleCredentials.fromStream(it) }
            }
            location.startsWith("file:") -> {
                FileInputStream(location.substring(5)).use { GoogleCredentials.fromStream(it) }
            }
            else -> {
                FileInputStream(location).use { GoogleCredentials.fromStream(it) }
            }
        }
    }
}
