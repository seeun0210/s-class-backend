package com.sclass.backoffice.config

import com.sclass.common.jwt.CurrentUserIdArgumentResolver
import com.sclass.common.jwt.JwtAuthInterceptor
import com.sclass.common.jwt.PlatformAuthInterceptor
import com.sclass.domain.domains.user.domain.Platform
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    private val jwtAuthInterceptor: JwtAuthInterceptor,
    private val currentUserIdArgumentResolver: CurrentUserIdArgumentResolver,
    @param:Value("\${cors.allow.origins}") private val allowedOrigins: String,
) : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry
            .addMapping("/**")
            .allowedOrigins(
                *allowedOrigins
                    .split(",")
                    .map(String::trim)
                    .filter(String::isNotEmpty)
                    .toTypedArray(),
            ).allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry
            .addInterceptor(jwtAuthInterceptor)
            .addPathPatterns("/api/**")
            .excludePathPatterns("/api/v1/auth/**")
        registry
            .addInterceptor(PlatformAuthInterceptor(Platform.BACKOFFICE.name))
            .addPathPatterns("/api/**")
            .excludePathPatterns("/api/v1/auth/**")
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(currentUserIdArgumentResolver)
    }
}
