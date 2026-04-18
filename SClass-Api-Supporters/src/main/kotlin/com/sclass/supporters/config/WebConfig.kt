package com.sclass.supporters.config

import com.sclass.common.jwt.CurrentUserIdArgumentResolver
import com.sclass.common.jwt.CurrentUserRoleArgumentResolver
import com.sclass.common.jwt.JwtAuthInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    private val jwtAuthInterceptor: JwtAuthInterceptor,
    private val currentUserIdArgumentResolver: CurrentUserIdArgumentResolver,
    private val currentUserRoleArgumentResolver: CurrentUserRoleArgumentResolver,
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry
            .addInterceptor(jwtAuthInterceptor)
            .addPathPatterns("/api/**")
            .excludePathPatterns(
                "/api/v1/auth/**",
                "/api/v1/oauth/**",
                "/api/v1/auth/phone/**",
                "/api/v1/payments/nicepay",
                "/api/v1/partnership-leads",
                "/api/v1/catalog/**",
            )
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(currentUserIdArgumentResolver)
        resolvers.add(currentUserRoleArgumentResolver)
    }
}
