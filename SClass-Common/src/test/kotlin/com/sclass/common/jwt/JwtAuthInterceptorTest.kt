package com.sclass.common.jwt

import com.sclass.common.exception.UnauthorizedException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

class JwtAuthInterceptorTest {
    private lateinit var jwtTokenProvider: JwtTokenProvider
    private lateinit var aesTokenEncryptor: AesTokenEncryptor
    private lateinit var interceptor: JwtAuthInterceptor

    private val jwtSecretKey = "test-secret-key-that-is-at-least-32-bytes-long!!"
    private val aesSecretKey = "test-aes-secret-key-32-bytes-ok!"

    @BeforeEach
    fun setUp() {
        jwtTokenProvider = JwtTokenProvider(JwtProperties(secretKey = jwtSecretKey, accessExp = 3600, refreshExp = 86400))
        aesTokenEncryptor = AesTokenEncryptor(TokenEncryptionProperties(secretKey = aesSecretKey))
        interceptor = JwtAuthInterceptor(jwtTokenProvider, aesTokenEncryptor)
    }

    @Test
    fun `유효한 Bearer 토큰이면 userId와 role을 request에 저장한다`() {
        val accessToken = jwtTokenProvider.generateAccessToken("user-123", "ADMIN")
        val encryptedToken = aesTokenEncryptor.encrypt(accessToken)

        val request = MockHttpServletRequest()
        request.addHeader("Authorization", "Bearer $encryptedToken")
        val response = MockHttpServletResponse()

        val result = interceptor.preHandle(request, response, Any())

        assertTrue(result)
        assertEquals("user-123", request.getAttribute(JwtAuthInterceptor.USER_ID_ATTRIBUTE))
        assertEquals("ADMIN", request.getAttribute(JwtAuthInterceptor.USER_ROLE_ATTRIBUTE))
    }

    @Test
    fun `Authorization 헤더가 없으면 UnauthorizedException이 발생한다`() {
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()

        assertThrows<UnauthorizedException> {
            interceptor.preHandle(request, response, Any())
        }
    }

    @Test
    fun `Bearer 접두사가 없으면 UnauthorizedException이 발생한다`() {
        val request = MockHttpServletRequest()
        request.addHeader("Authorization", "Basic some-token")
        val response = MockHttpServletResponse()

        assertThrows<UnauthorizedException> {
            interceptor.preHandle(request, response, Any())
        }
    }
}
