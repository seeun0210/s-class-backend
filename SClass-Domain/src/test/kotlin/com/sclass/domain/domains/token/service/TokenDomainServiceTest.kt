package com.sclass.domain.domains.token.service

import com.sclass.common.jwt.AesTokenEncryptor
import com.sclass.common.jwt.JwtTokenProvider
import com.sclass.common.jwt.VerificationTokenInfo
import com.sclass.common.jwt.exception.RefreshTokenRevokedException
import com.sclass.domain.domains.token.adaptor.RefreshTokenAdaptor
import com.sclass.domain.domains.token.domain.RefreshToken
import com.sclass.domain.domains.user.domain.AuthProvider
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.Role
import com.sclass.domain.domains.verification.domain.VerificationChannel
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
import org.junit.jupiter.api.assertThrows

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
            every { jwtTokenProvider.generateAccessToken("user-id", "STUDENT", "SUPPORTERS") } returns "raw-access"
            every { jwtTokenProvider.generateRefreshToken("user-id") } returns "raw-refresh"
            every { jwtTokenProvider.getRefreshTokenTtlSecond() } returns 604800L
            every { aesTokenEncryptor.encrypt("raw-access") } returns "encrypted-access"
            every { aesTokenEncryptor.encrypt("raw-refresh") } returns "encrypted-refresh"
            every { refreshTokenAdaptor.save(any()) } returns mockk()

            val result = tokenDomainService.issueTokens("user-id", Role.STUDENT, Platform.SUPPORTERS)

            assertEquals("encrypted-access", result.accessToken)
            assertEquals("encrypted-refresh", result.refreshToken)
        }

        @Test
        fun `RefreshToken이 저장된다`() {
            val refreshTokenSlot = slot<RefreshToken>()
            every { jwtTokenProvider.generateAccessToken("user-id", "STUDENT", "SUPPORTERS") } returns "raw-access"
            every { jwtTokenProvider.generateRefreshToken("user-id") } returns "raw-refresh"
            every { jwtTokenProvider.getRefreshTokenTtlSecond() } returns 604800L
            every { aesTokenEncryptor.encrypt(any()) } returns "encrypted"
            every { refreshTokenAdaptor.save(capture(refreshTokenSlot)) } returns mockk()

            tokenDomainService.issueTokens("user-id", Role.STUDENT, Platform.SUPPORTERS)

            assertEquals("user-id", refreshTokenSlot.captured.userId)
        }
    }

    @Nested
    inner class ResolveUserId {
        @Test
        fun `유효한 refresh token이 DB에 존재하면 userId를 반환한다`() {
            every { aesTokenEncryptor.decrypt("encrypted-refresh") } returns "raw-refresh"
            every { jwtTokenProvider.parseRefreshToken("raw-refresh") } returns "user-id"
            every { refreshTokenAdaptor.existsValidByUserId("user-id") } returns true

            val result = tokenDomainService.resolveUserId("encrypted-refresh")

            assertEquals("user-id", result)
        }

        @Test
        fun `DB에 유효한 refresh token이 없으면 RefreshTokenRevokedException을 던진다`() {
            every { aesTokenEncryptor.decrypt("encrypted-refresh") } returns "raw-refresh"
            every { jwtTokenProvider.parseRefreshToken("raw-refresh") } returns "user-id"
            every { refreshTokenAdaptor.existsValidByUserId("user-id") } returns false

            assertThrows<RefreshTokenRevokedException> {
                tokenDomainService.resolveUserId("encrypted-refresh")
            }
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

    @Nested
    inner class IssueVerificationToken {
        @Test
        fun `verification JWT를 생성하고 암호화하여 반환한다`() {
            every {
                jwtTokenProvider.generateVerificationToken("PHONE", "010-1234-5678")
            } returns "raw-verification-jwt"
            every { aesTokenEncryptor.encrypt("raw-verification-jwt") } returns "encrypted-verification"

            val result =
                tokenDomainService.issueVerificationToken(
                    channel = VerificationChannel.PHONE,
                    target = "010-1234-5678",
                )

            assertEquals("encrypted-verification", result)
        }
    }

    @Nested
    inner class ResolveVerificationToken {
        @Test
        fun `암호화된 verification token에서 channel과 target을 추출한다`() {
            every { aesTokenEncryptor.decrypt("encrypted-verification") } returns "raw-verification-jwt"
            every { jwtTokenProvider.parseVerificationToken("raw-verification-jwt") } returns
                VerificationTokenInfo(channel = "PHONE", target = "010-1234-5678")

            val result = tokenDomainService.resolveVerificationToken("encrypted-verification")

            assertEquals("PHONE", result.channel)
            assertEquals("010-1234-5678", result.target)
        }
    }
}
