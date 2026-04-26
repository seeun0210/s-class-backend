package com.sclass.supporters.oauth.usecase

import com.sclass.common.exception.ForbiddenException
import com.sclass.common.exception.GoogleRefreshTokenMissingException
import com.sclass.common.jwt.AesTokenEncryptor
import com.sclass.domain.domains.oauth.adaptor.TeacherGoogleAccountAdaptor
import com.sclass.domain.domains.oauth.domain.TeacherGoogleAccount
import com.sclass.domain.domains.user.domain.Role
import com.sclass.infrastructure.oauth.client.GoogleAuthorizationCodeClient
import com.sclass.infrastructure.oauth.dto.GoogleTokenExchangeResponse
import com.sclass.infrastructure.oauth.dto.GoogleUserInfoResponse
import com.sclass.supporters.oauth.dto.ConnectGoogleRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId

class ConnectGoogleUseCaseTest {
    private lateinit var googleClient: GoogleAuthorizationCodeClient
    private lateinit var accountAdaptor: TeacherGoogleAccountAdaptor
    private lateinit var encryptor: AesTokenEncryptor
    private lateinit var useCase: ConnectGoogleUseCase

    private val userId = "user-id-00000000000000001"
    private val redirectUri = "http://localhost:3000/oauth/google/callback"

    private val fixedNow = LocalDateTime.of(2026, 4, 26, 14, 0)
    private val clock =
        Clock.fixed(
            fixedNow.atZone(ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault(),
        )

    @BeforeEach
    fun setUp() {
        googleClient = mockk()
        accountAdaptor = mockk()
        encryptor = mockk()
        useCase = ConnectGoogleUseCase(googleClient, accountAdaptor, encryptor, clock)

        every { encryptor.encrypt(any()) } answers { "encrypted-${firstArg<String>()}" }
        every { accountAdaptor.save(any()) } answers { firstArg() }
    }

    private fun tokenResponse(refreshToken: String? = "refresh-token-xyz") =
        GoogleTokenExchangeResponse(
            accessToken = "access-token-abc",
            expiresIn = 3600,
            refreshToken = refreshToken,
            scope = "https://www.googleapis.com/auth/calendar.events",
            tokenType = "Bearer",
        )

    private fun userInfo() =
        GoogleUserInfoResponse(
            id = "google-user-id",
            email = "teacher@gmail.com",
            verifiedEmail = true,
            name = "Teacher",
        )

    @Test
    fun `신규 연결 시 새 TeacherGoogleAccount가 저장된다`() {
        every { googleClient.exchangeCodeForTokens("code-1", redirectUri) } returns tokenResponse()
        every { googleClient.fetchUserInfo("access-token-abc") } returns userInfo()
        every { accountAdaptor.findByUserIdOrNull(userId) } returns null

        val response =
            useCase.execute(
                userId,
                Role.TEACHER,
                ConnectGoogleRequest(code = "code-1", redirectUri = redirectUri),
            )

        assertAll(
            { assertTrue(response.connected) },
            { assertEquals("teacher@gmail.com", response.googleEmail) },
            { assertEquals(fixedNow, response.connectedAt) },
            { assertEquals("https://www.googleapis.com/auth/calendar.events", response.scope) },
        )
        verify { encryptor.encrypt("refresh-token-xyz") }
        verify {
            accountAdaptor.save(
                match<TeacherGoogleAccount> {
                    it.userId == userId &&
                        it.googleEmail == "teacher@gmail.com" &&
                        it.encryptedRefreshToken == "encrypted-refresh-token-xyz" &&
                        it.connectedAt == fixedNow
                },
            )
        }
    }

    @Test
    fun `기존 연결이 있으면 reconnect로 토큰과 이메일이 갱신된다`() {
        val existing =
            TeacherGoogleAccount(
                userId = userId,
                googleEmail = "old@gmail.com",
                encryptedRefreshToken = "encrypted-old-token",
                scope = "old-scope",
                connectedAt = fixedNow.minusDays(30),
            )
        every {
            googleClient.exchangeCodeForTokens("code-2", redirectUri)
        } returns tokenResponse(refreshToken = "new-refresh-token")
        every { googleClient.fetchUserInfo("access-token-abc") } returns userInfo()
        every { accountAdaptor.findByUserIdOrNull(userId) } returns existing

        useCase.execute(
            userId,
            Role.TEACHER,
            ConnectGoogleRequest(code = "code-2", redirectUri = redirectUri),
        )

        assertAll(
            { assertEquals("teacher@gmail.com", existing.googleEmail) },
            { assertEquals("encrypted-new-refresh-token", existing.encryptedRefreshToken) },
            { assertEquals("https://www.googleapis.com/auth/calendar.events", existing.scope) },
            { assertEquals(fixedNow.minusDays(30), existing.connectedAt) },
        )
        verify { accountAdaptor.save(existing) }
    }

    @Test
    fun `Google이 refresh_token을 안 주면 GoogleRefreshTokenMissingException`() {
        every {
            googleClient.exchangeCodeForTokens("code-3", redirectUri)
        } returns tokenResponse(refreshToken = null)

        assertThrows<GoogleRefreshTokenMissingException> {
            useCase.execute(
                userId,
                Role.TEACHER,
                ConnectGoogleRequest(code = "code-3", redirectUri = redirectUri),
            )
        }
        verify(exactly = 0) { accountAdaptor.save(any()) }
    }

    @Test
    fun `선생님 권한이 아니면 Google 계정을 연결할 수 없다`() {
        assertThrows<ForbiddenException> {
            useCase.execute(
                userId,
                Role.STUDENT,
                ConnectGoogleRequest(code = "code-3", redirectUri = redirectUri),
            )
        }

        verify(exactly = 0) { googleClient.exchangeCodeForTokens(any(), any()) }
        verify(exactly = 0) { accountAdaptor.save(any()) }
    }

    @Test
    fun `refresh token은 항상 암호화되어 저장된다`() {
        every { googleClient.exchangeCodeForTokens(any(), any()) } returns tokenResponse(refreshToken = "raw-token")
        every { googleClient.fetchUserInfo(any()) } returns userInfo()
        every { accountAdaptor.findByUserIdOrNull(userId) } returns null

        useCase.execute(
            userId,
            Role.TEACHER,
            ConnectGoogleRequest(code = "code-4", redirectUri = redirectUri),
        )

        verify { encryptor.encrypt("raw-token") }
        verify {
            accountAdaptor.save(
                match<TeacherGoogleAccount> {
                    it.encryptedRefreshToken == "encrypted-raw-token" &&
                        it.encryptedRefreshToken != "raw-token"
                },
            )
        }
    }
}
