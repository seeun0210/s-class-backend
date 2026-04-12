package com.sclass.common.jwt

import com.sclass.common.jwt.exception.InvalidTokenException
import com.sclass.common.jwt.exception.TokenExpiredException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.nio.charset.StandardCharsets
import java.util.Date

class JwtTokenProviderTest {
    private lateinit var provider: JwtTokenProvider

    private val secretKey = "test-secret-key-that-is-at-least-32-bytes-long!!"

    @BeforeEach
    fun setUp() {
        val properties = JwtProperties(secretKey = secretKey, accessExp = 3600, refreshExp = 86400)
        provider = JwtTokenProvider(properties)
    }

    @Test
    fun `access token 생성 후 파싱하면 userId와 role이 복원된다`() {
        val token = provider.generateAccessToken("user-123", "STUDENT")

        val info = provider.parseAccessToken(token)

        assertEquals("user-123", info.userId)
        assertEquals("STUDENT", info.role)
    }

    @Test
    fun `refresh token 생성 후 파싱하면 userId가 복원된다`() {
        val token = provider.generateRefreshToken("user-456")

        val userId = provider.parseRefreshToken(token)

        assertEquals("user-456", userId)
    }

    @Test
    fun `signup token 생성 후 파싱하면 모든 claim이 복원된다`() {
        val token =
            provider.generateSignupToken(
                oauthId = "oauth-id",
                provider = "GOOGLE",
                email = "test@example.com",
                name = "테스트",
                role = "STUDENT",
                platform = "SUPPORTERS",
            )

        val info = provider.parseSignupToken(token)

        assertEquals("oauth-id", info.oauthId)
        assertEquals("GOOGLE", info.provider)
        assertEquals("test@example.com", info.email)
        assertEquals("테스트", info.name)
        assertEquals("STUDENT", info.role)
        assertEquals("SUPPORTERS", info.platform)
    }

    @Test
    fun `만료된 토큰을 파싱하면 TokenExpiredException이 발생한다`() {
        val key = Keys.hmacShaKeyFor(secretKey.toByteArray(StandardCharsets.UTF_8))
        val expiredToken =
            Jwts
                .builder()
                .issuer("sclass")
                .subject("user-id")
                .claim("type", "ACCESS")
                .claim("role", "STUDENT")
                .issuedAt(Date(System.currentTimeMillis() - 7200_000))
                .expiration(Date(System.currentTimeMillis() - 3600_000))
                .signWith(key)
                .compact()

        assertThrows<TokenExpiredException> {
            provider.parseAccessToken(expiredToken)
        }
    }

    @Test
    fun `refresh token으로 access token 파싱을 시도하면 InvalidTokenException이 발생한다`() {
        val refreshToken = provider.generateRefreshToken("user-id")

        assertThrows<InvalidTokenException> {
            provider.parseAccessToken(refreshToken)
        }
    }

    @Test
    fun `access token으로 refresh token 파싱을 시도하면 InvalidTokenException이 발생한다`() {
        val accessToken = provider.generateAccessToken("user-id", "STUDENT")

        assertThrows<InvalidTokenException> {
            provider.parseRefreshToken(accessToken)
        }
    }

    @Test
    fun `잘못된 형식의 토큰을 파싱하면 InvalidTokenException이 발생한다`() {
        assertThrows<InvalidTokenException> {
            provider.parseAccessToken("invalid-token")
        }
    }
}
