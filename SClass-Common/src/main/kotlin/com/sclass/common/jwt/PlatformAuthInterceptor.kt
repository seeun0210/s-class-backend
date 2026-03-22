package com.sclass.common.jwt

import com.sclass.common.exception.ForbiddenException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.servlet.HandlerInterceptor

class PlatformAuthInterceptor(
    private val allowedPlatform: String,
) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        val platform =
            request.getAttribute(JwtAuthInterceptor.USER_PLATFORM_ATTRIBUTE) as? String
                ?: throw ForbiddenException()
        if (platform != allowedPlatform) {
            throw ForbiddenException()
        }
        return true
    }
}
