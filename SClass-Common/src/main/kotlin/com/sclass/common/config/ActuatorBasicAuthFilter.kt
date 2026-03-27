package com.sclass.common.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.Base64

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnProperty("management.auth.username")
class ActuatorBasicAuthFilter(
    private val actuatorAuthProperties: ActuatorAuthProperties,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val path = request.requestURI

        if (!path.startsWith("/actuator")) {
            filterChain.doFilter(request, response)
            return
        }

        // /actuator/health는 App Runner 헬스체크를 위해 인증 없이 허용
        if (path == "/actuator/health") {
            filterChain.doFilter(request, response)
            return
        }

        val authHeader = request.getHeader("Authorization")
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            sendUnauthorized(response)
            return
        }

        val decoded = String(Base64.getDecoder().decode(authHeader.substring(6)))
        val parts = decoded.split(":", limit = 2)
        if (parts.size != 2 ||
            parts[0] != actuatorAuthProperties.username ||
            parts[1] != actuatorAuthProperties.password
        ) {
            sendUnauthorized(response)
            return
        }

        filterChain.doFilter(request, response)
    }

    private fun sendUnauthorized(response: HttpServletResponse) {
        response.setHeader("WWW-Authenticate", "Basic realm=\"Actuator\"")
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
    }
}
