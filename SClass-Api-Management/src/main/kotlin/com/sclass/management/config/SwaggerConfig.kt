package com.sclass.management.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
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
                    .title("S-Class Management API")
                    .description("관리자/선생님용 LMS API")
                    .version("v1"),
            ).addSecurityItem(SecurityRequirement().addList(securitySchemeName))
            .components(Components().addSecuritySchemes(securitySchemeName, securityScheme))
    }
}
