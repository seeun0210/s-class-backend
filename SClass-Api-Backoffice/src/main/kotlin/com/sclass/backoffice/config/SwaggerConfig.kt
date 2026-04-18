package com.sclass.backoffice.config

import com.sclass.common.annotation.Public
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springdoc.core.customizers.OperationCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {
    @Bean
    fun openAPI(): OpenAPI {
        val securitySchemeName = "Bearer Authentication"
        val securityScheme =
            SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")

        return OpenAPI()
            .info(
                Info()
                    .title("S-Class Backoffice API")
                    .description("슈퍼어드민 Backoffice API")
                    .version("v1"),
            ).addSecurityItem(SecurityRequirement().addList(securitySchemeName))
            .components(Components().addSecuritySchemes(securitySchemeName, securityScheme))
    }

    @Bean
    fun publicEndpointCustomizer(): OperationCustomizer =
        OperationCustomizer { operation, handlerMethod ->
            val isPublic =
                handlerMethod.hasMethodAnnotation(Public::class.java) ||
                    handlerMethod.beanType.isAnnotationPresent(Public::class.java)
            if (isPublic) operation.security = emptyList()
            operation
        }
}
