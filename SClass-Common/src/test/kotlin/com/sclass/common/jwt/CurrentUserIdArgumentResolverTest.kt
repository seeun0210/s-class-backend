package com.sclass.common.jwt

import com.sclass.common.exception.UnauthorizedException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.ServletWebRequest

class CurrentUserIdArgumentResolverTest {
    private lateinit var resolver: CurrentUserIdArgumentResolver

    @BeforeEach
    fun setUp() {
        resolver = CurrentUserIdArgumentResolver()
    }

    @Test
    fun `request에 userId가 있으면 반환한다`() {
        val httpRequest = MockHttpServletRequest()
        httpRequest.setAttribute(JwtAuthInterceptor.USER_ID_ATTRIBUTE, "user-123")
        val webRequest = ServletWebRequest(httpRequest)

        val result = resolver.resolveArgument(mockParameter(), null, webRequest, null)

        assertEquals("user-123", result)
    }

    @Test
    fun `request에 userId가 없으면 UnauthorizedException이 발생한다`() {
        val httpRequest = MockHttpServletRequest()
        val webRequest = ServletWebRequest(httpRequest)

        assertThrows<UnauthorizedException> {
            resolver.resolveArgument(mockParameter(), null, webRequest, null)
        }
    }

    private fun mockParameter(): org.springframework.core.MethodParameter {
        val method = CurrentUserIdArgumentResolverTest::class.java.getDeclaredMethod("dummyMethod", String::class.java)
        return org.springframework.core.MethodParameter(method, 0)
    }

    @Suppress("unused")
    private fun dummyMethod(
        @com.sclass.common.annotation.CurrentUserId userId: String,
    ) {
    }
}
