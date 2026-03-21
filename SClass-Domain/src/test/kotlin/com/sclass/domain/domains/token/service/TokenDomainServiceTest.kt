package com.sclass.domain.domains.token.service

import com.sclass.common.jwt.AesTokenEncryptor
import com.sclass.common.jwt.JwtTokenProvider
import com.sclass.domain.domains.token.adaptor.RefreshTokenAdaptor
import com.sclass.domain.domains.token.domain.RefreshToken
import com.sclass.domain.domains.user.domain.AuthProvider
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.Role
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TokenDomainServiceTest {
    private lateinit var jwtTokenProvider: JwtTokenProvider
    private lateinit var aesTokenEncryptor: AesTokenEncryptor
    private lateinit var refreshTokenAdaptor: RefreshTokenAdaptor
    private lateinit var tokenDomainService: TokenDomainService

    @BeforeEach
    fun setUp() {
        jwtTokenProvider = mockk()
        aesTokenEncryptor = mockk()
        refreshTokenAdaptor = mockk()
        tokenDomainService = TokenDomainService(jwtTokenProvider, aesTokenEncryptor, refreshTokenAdaptor)
    }

    @Nested
    inner class IssueTokens {
        @Test
        fun `암호화된 access token과 refresh token을 반환한다`() {
            every { jwtTokenProvider.generateAccessToken("user-id", "STUDENT") } returns "raw-access"
            every { jwtTokenProvider.generateRefreshToken("user-id") } returns "raw-refresh"
            every { jwtTokenProvider.getRefreshTokenTtlSecond() } returns 604800L
            every { aesTokenEncryptor.encrypt("raw-access") } returns "encrypted-access"
            every { aesTokenEncryptor.encrypt("raw-refresh") } returns "encrypted-refresh"
            every { refreshTokenAdaptor.save(any()) } returns mockk()

            val result = tokenDomainService.issueTokens("user-id", Role.STUDENT)

            assertEquals("encrypted-access", result.accessToken)
            assertEquals("encrypted-refresh", result.refreshToken)
        }

        @Test
        fun `RefreshToken이 저장된다`() {
            val refreshTokenSlot = slot<RefreshToken>()
            every { jwtTokenProvider.generateAccessToken("user-id", "STUDENT") } returns "raw-access"
            every { jwtTokenProvider.generateRefreshToken("user-id") } returns "raw-refresh"
            every { jwtTokenProvider.getRefreshTokenTtlSecond() } returns 604800L
            every { aesTokenEncryptor.encrypt(any()) } returns "encrypted"
            every { refreshTokenAdaptor.save(capture(refreshTokenSlot)) } returns mockk()

            tokenDomainService.issueTokens("user-id", Role.STUDENT)

            assertEquals("user-id", refreshTokenSlot.captured.userId)
        }
    }

    @Nested
    inner class ResolveUserId {
        @Test
        fun `암호화된 refresh token에서 userId를 추출한다`() {
            every { aesTokenEncryptor.decrypt("encrypted-refresh") } returns "raw-refresh"
            every { jwtTokenProvider.parseRefreshToken("raw-refresh") } returns "user-id"

            val result = tokenDomainService.resolveUserId("encrypted-refresh")

            assertEquals("user-id", result)
        }
    }

    @Nested
    inner class RevokeAllByUserId {
        @Test
        fun `deleteAllByUserId를 호출한다`() {
            every { refreshTokenAdaptor.deleteAllByUserId("user-id") } just runs

            tokenDomainService.revokeAllByUserId("user-id")

            verify { refreshTokenAdaptor.deleteAllByUserId("user-id") }
        }
    }

    @Nested
    inner class IssueSignupToken {
        @Test
        fun `signup JWT를 생성하고 암호화하여 반환한다`() {
            every {
                jwtTokenProvider.generateSignupToken("oauth-id", "GOOGLE", "test@example.com", "테스트", "STUDENT", "SUPPORTERS")
            } returns "raw-signup-jwt"
            every { aesTokenEncryptor.encrypt("raw-signup-jwt") } returns "encrypted-signup"

            val result =
                tokenDomainService.issueSignupToken(
                    oauthId = "oauth-id",
                    provider = AuthProvider.GOOGLE,
                    email = "test@example.com",
                    name = "테스트",
                    role = Role.STUDENT,
                    platform = Platform.SUPPORTERS,
                )

            assertEquals("encrypted-signup", result)
        }
    }
}
