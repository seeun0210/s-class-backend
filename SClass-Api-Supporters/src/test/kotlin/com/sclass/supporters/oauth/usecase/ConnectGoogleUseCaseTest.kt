package com.sclass.supporters.oauth.usecase

import com.sclass.common.exception.ForbiddenException
import com.sclass.common.exception.GoogleCalendarScopeMissingException
import com.sclass.common.exception.GoogleIdentityScopeMissingException
import com.sclass.common.exception.GoogleRefreshTokenMissingException
import com.sclass.common.jwt.AesTokenEncryptor
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
import java.time.LocalDateTime

class ConnectGoogleUseCaseTest {
    private lateinit var googleClient: GoogleAuthorizationCodeClient
    private lateinit var connectGoogleAccountLockedUseCase: ConnectGoogleAccountLockedUseCase
    private lateinit var encryptor: AesTokenEncryptor
    private lateinit var useCase: ConnectGoogleUseCase

    private val userId = "user-id-00000000000000001"
    private val redirectUri = "http://localhost:3000/oauth/google/callback"
    private val grantedScope =
        listOf(
            "openid",
            "https://www.googleapis.com/auth/userinfo.email",
            "https://www.googleapis.com/auth/calendar.events",
        ).joinToString(" ")

    private val fixedNow = LocalDateTime.of(2026, 4, 26, 14, 0)

    @BeforeEach
    fun setUp() {
        googleClient = mockk()
        connectGoogleAccountLockedUseCase = mockk()
        encryptor = mockk()
        useCase = ConnectGoogleUseCase(googleClient, connectGoogleAccountLockedUseCase, encryptor)

        every { encryptor.encrypt(any()) } answers { "encrypted-${firstArg<String>()}" }
    }

    private fun tokenResponse(
        refreshToken: String? = "refresh-token-xyz",
        scope: String = grantedScope,
    ) = GoogleTokenExchangeResponse(
        accessToken = "access-token-abc",
        expiresIn = 3600,
        refreshToken = refreshToken,
        scope = scope,
        tokenType = "Bearer",
    )

    private fun userInfo() =
        GoogleUserInfoResponse(
            id = "google-user-id",
            email = "teacher@gmail.com",
            verifiedEmail = true,
            name = "Teacher",
        )

    private fun connectedAccount(
        googleEmail: String = "teacher@gmail.com",
        encryptedRefreshToken: String = "encrypted-refresh-token-xyz",
        scope: String = grantedScope,
        connectedAt: LocalDateTime = fixedNow,
    ) = TeacherGoogleAccount(
        userId = userId,
        googleEmail = googleEmail,
        encryptedRefreshToken = encryptedRefreshToken,
        scope = scope,
        connectedAt = connectedAt,
    )

    @Test
    fun `신규 연결 시 새 TeacherGoogleAccount가 저장된다`() {
        every { googleClient.exchangeCodeForTokens("code-1", redirectUri) } returns tokenResponse()
        every { googleClient.fetchUserInfo("access-token-abc") } returns userInfo()
        every {
            connectGoogleAccountLockedUseCase.execute(
                userId = userId,
                googleEmail = "teacher@gmail.com",
                encryptedRefreshToken = "encrypted-refresh-token-xyz",
                scope = grantedScope,
            )
        } returns connectedAccount()

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
            { assertEquals(grantedScope, response.scope) },
        )
        verify { encryptor.encrypt("refresh-token-xyz") }
        verify {
            connectGoogleAccountLockedUseCase.execute(
                userId = userId,
                googleEmail = "teacher@gmail.com",
                encryptedRefreshToken = "encrypted-refresh-token-xyz",
                scope = grantedScope,
            )
        }
    }

    @Test
    fun `locked use case가 반환한 계정으로 연결 응답을 만든다`() {
        every {
            googleClient.exchangeCodeForTokens("code-2", redirectUri)
        } returns tokenResponse(refreshToken = "new-refresh-token")
        every { googleClient.fetchUserInfo("access-token-abc") } returns userInfo()
        every {
            connectGoogleAccountLockedUseCase.execute(
                userId = userId,
                googleEmail = "teacher@gmail.com",
                encryptedRefreshToken = "encrypted-new-refresh-token",
                scope = grantedScope,
            )
        } returns
            connectedAccount(
                googleEmail = "teacher@gmail.com",
                encryptedRefreshToken = "encrypted-new-refresh-token",
                connectedAt = fixedNow.minusDays(30),
            )

        val response =
            useCase.execute(
                userId,
                Role.TEACHER,
                ConnectGoogleRequest(code = "code-2", redirectUri = redirectUri),
            )

        assertAll(
            { assertTrue(response.connected) },
            { assertEquals("teacher@gmail.com", response.googleEmail) },
            { assertEquals(grantedScope, response.scope) },
            { assertEquals(fixedNow.minusDays(30), response.connectedAt) },
        )
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
        verify(exactly = 0) { connectGoogleAccountLockedUseCase.execute(any(), any(), any(), any()) }
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
        verify(exactly = 0) { connectGoogleAccountLockedUseCase.execute(any(), any(), any(), any()) }
    }

    @Test
    fun `Google email scope가 없으면 userinfo 호출 전에 실패한다`() {
        every {
            googleClient.exchangeCodeForTokens("code-5", redirectUri)
        } returns tokenResponse(scope = "https://www.googleapis.com/auth/calendar.events")

        assertThrows<GoogleIdentityScopeMissingException> {
            useCase.execute(
                userId,
                Role.TEACHER,
                ConnectGoogleRequest(code = "code-5", redirectUri = redirectUri),
            )
        }

        verify(exactly = 0) { googleClient.fetchUserInfo(any()) }
        verify(exactly = 0) { connectGoogleAccountLockedUseCase.execute(any(), any(), any(), any()) }
    }

    @Test
    fun `openid만 있고 email scope가 없으면 userinfo 호출 전에 실패한다`() {
        every {
            googleClient.exchangeCodeForTokens("code-6", redirectUri)
        } returns tokenResponse(scope = "openid profile https://www.googleapis.com/auth/calendar.events")

        assertThrows<GoogleIdentityScopeMissingException> {
            useCase.execute(
                userId,
                Role.TEACHER,
                ConnectGoogleRequest(code = "code-6", redirectUri = redirectUri),
            )
        }

        verify(exactly = 0) { googleClient.fetchUserInfo(any()) }
        verify(exactly = 0) { connectGoogleAccountLockedUseCase.execute(any(), any(), any(), any()) }
    }

    @Test
    fun `calendar scope가 없으면 userinfo 호출 전에 실패한다`() {
        every {
            googleClient.exchangeCodeForTokens("code-7", redirectUri)
        } returns tokenResponse(scope = "openid email https://www.googleapis.com/auth/userinfo.email")

        assertThrows<GoogleCalendarScopeMissingException> {
            useCase.execute(
                userId,
                Role.TEACHER,
                ConnectGoogleRequest(code = "code-7", redirectUri = redirectUri),
            )
        }

        verify(exactly = 0) { googleClient.fetchUserInfo(any()) }
        verify(exactly = 0) { connectGoogleAccountLockedUseCase.execute(any(), any(), any(), any()) }
    }

    @Test
    fun `refresh token은 항상 암호화되어 저장된다`() {
        every { googleClient.exchangeCodeForTokens(any(), any()) } returns tokenResponse(refreshToken = "raw-token")
        every { googleClient.fetchUserInfo(any()) } returns userInfo()
        every {
            connectGoogleAccountLockedUseCase.execute(
                userId = userId,
                googleEmail = "teacher@gmail.com",
                encryptedRefreshToken = "encrypted-raw-token",
                scope = grantedScope,
            )
        } returns connectedAccount(encryptedRefreshToken = "encrypted-raw-token")

        useCase.execute(
            userId,
            Role.TEACHER,
            ConnectGoogleRequest(code = "code-4", redirectUri = redirectUri),
        )

        verify { encryptor.encrypt("raw-token") }
        verify {
            connectGoogleAccountLockedUseCase.execute(
                userId = userId,
                googleEmail = "teacher@gmail.com",
                encryptedRefreshToken = "encrypted-raw-token",
                scope = grantedScope,
            )
        }
    }
}
