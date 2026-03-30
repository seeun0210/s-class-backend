package com.sclass.common.jwt

import com.sclass.common.annotation.CurrentUserRole
import com.sclass.common.exception.UnauthorizedException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import kotlin.jvm.java

@Component
class CurrentUserRoleArgumentResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean = parameter.hasParameterAnnotation(CurrentUserRole::class.java)

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): String {
        val request = webRequest.getNativeRequest(HttpServletRequest::class.java)!!
        return request.getAttribute(JwtAuthInterceptor.USER_ROLE_ATTRIBUTE) as? String
            ?: throw UnauthorizedException()
    }
}
