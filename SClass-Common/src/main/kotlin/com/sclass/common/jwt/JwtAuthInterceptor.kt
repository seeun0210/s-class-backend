package com.sclass.common.jwt

import com.sclass.common.annotation.Public
import com.sclass.common.exception.UnauthorizedException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

@Component
class JwtAuthInterceptor(
    private val jwtTokenProvider: JwtTokenProvider,
    private val aesTokenEncryptor: AesTokenEncryptor,
) : HandlerInterceptor {
    companion object {
        const val USER_ID_ATTRIBUTE = "currentUserId"
        const val USER_ROLE_ATTRIBUTE = "currentUserRole"
        const val USER_PLATFORM_ATTRIBUTE = "currentUserPlatform"
    }

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        if (request.method ==
            org.springframework.http.HttpMethod.OPTIONS
                .name()
        ) {
            return true
        }

        if (handler is HandlerMethod && handler.isPublic()) return true

        val authHeader = request.getHeader("Authorization") ?: throw UnauthorizedException()
        if (!authHeader.startsWith("Bearer ")) throw UnauthorizedException()

        val encryptedToken = authHeader.substring(7)
        val jwt = aesTokenEncryptor.decrypt(encryptedToken)
        val tokenInfo = jwtTokenProvider.parseAccessToken(jwt)

        request.setAttribute(USER_ID_ATTRIBUTE, tokenInfo.userId)
        request.setAttribute(USER_ROLE_ATTRIBUTE, tokenInfo.role)
        return true
    }

    private fun HandlerMethod.isPublic(): Boolean =
        hasMethodAnnotation(Public::class.java) ||
            beanType.isAnnotationPresent(Public::class.java)
}
