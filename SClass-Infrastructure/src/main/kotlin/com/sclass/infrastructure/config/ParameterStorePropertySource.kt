package com.sclass.infrastructure.config

import org.springframework.boot.EnvironmentPostProcessor
import org.springframework.boot.SpringApplication
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MapPropertySource
import software.amazon.awssdk.services.ssm.SsmClient
import software.amazon.awssdk.services.ssm.model.GetParametersByPathRequest

class ParameterStorePropertySource : EnvironmentPostProcessor {
    private val logger =
        java.util.logging.Logger
            .getLogger(ParameterStorePropertySource::class.java.name)

    override fun postProcessEnvironment(
        environment: ConfigurableEnvironment,
        application: SpringApplication,
    ) {
        val prefix = System.getenv("SSM_PARAMETER_PREFIX") ?: return

        logger.info("Loading SSM parameters from prefix: $prefix (region: ${System.getenv("AWS_REGION")})")

        val parameters = mutableMapOf<String, Any>()
        val client = SsmClient.create()

        try {
            var nextToken: String? = null
            do {
                val request =
                    GetParametersByPathRequest
                        .builder()
                        .path(prefix)
                        .recursive(true)
                        .withDecryption(true)
                        .apply { if (nextToken != null) nextToken(nextToken) }
                        .build()

                val response = client.getParametersByPath(request)

                response.parameters().forEach { param ->
                    val key = param.name().removePrefix("$prefix/")
                    parameters[key] = param.value()
                }

                nextToken = response.nextToken()
            } while (nextToken != null)

            logger.info("Loaded ${parameters.size} SSM parameters: ${parameters.keys}")
        } catch (e: Exception) {
            logger.severe("Failed to load SSM parameters from $prefix: ${e.message}")
            throw e
        } finally {
            client.close()
        }

        if (parameters.isNotEmpty()) {
            environment.propertySources.addFirst(
                MapPropertySource("ssmParameterStore", parameters),
            )
        }
    }
}
