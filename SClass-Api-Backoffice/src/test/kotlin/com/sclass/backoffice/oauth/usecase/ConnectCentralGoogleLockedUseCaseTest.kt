package com.sclass.backoffice.oauth.usecase

import com.sclass.backoffice.oauth.dto.ConnectCentralGoogleRequest
import com.sclass.common.exception.GoogleCalendarCentralDisabledException
import com.sclass.common.exception.GoogleDriveScopeMissingException
import com.sclass.common.exception.GoogleOAuthStateInvalidException
import com.sclass.common.jwt.AesTokenEncryptor
import com.sclass.domain.domains.oauth.domain.CentralGoogleAccount
import com.sclass.domain.domains.oauth.exception.CentralGoogleAccountEmailNotAllowedException
import com.sclass.infrastructure.calendar.GoogleCentralCalendarProperties
import com.sclass.infrastructure.oauth.client.CentralGoogleAuthorizationCodeClient
import com.sclass.infrastructure.oauth.dto.GoogleTokenExchangeResponse
import com.sclass.infrastructure.oauth.dto.GoogleUserInfoResponse
import com.sclass.infrastructure.oauth.state.GoogleOAuthStateStore
import com.sclass.infrastructure.redis.DistributedLock
import com.sclass.infrastructure.redis.LockKey
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class ConnectCentralGoogleLockedUseCaseTest {
    private val grantedScope = CentralGoogleOAuthScopes.authorizationScopes.joinToString(" ")

    private val googleClient: CentralGoogleAuthorizationCodeClient = mockk()
    private val connectCentralGoogleAccountLockedUseCase: ConnectCentralGoogleAccountLockedUseCase = mockk()
    private val encryptor: AesTokenEncryptor = mockk()
    private val stateStore: GoogleOAuthStateStore = mockk()
    private val fixedNow = LocalDateTime.of(2026, 4, 26, 14, 0)
    private val properties =
        GoogleCentralCalendarProperties().apply {
            enabled = true
            allowedEmail = "central-google@example.com"
        }
    private val useCase =
        ConnectCentralGoogleLockedUseCase(
            googleClient = googleClient,
            connectCentralGoogleAccountLockedUseCase = connectCentralGoogleAccountLockedUseCase,
            encryptor = encryptor,
            stateStore = stateStore,
            properties = properties,
        )

    @Test
    fun `허용된 중앙 Google 계정이면 refresh token을 암호화하고 locked usecase에 저장을 위임한다`() {
        val request = ConnectCentralGoogleRequest(code = "auth-code", redirectUri = "https://backoffice/callback", state = "state")
        val account =
            CentralGoogleAccount(
                googleEmail = "central-google@example.com",
                encryptedRefreshToken = "encrypted-refresh-token",
                scope = grantedScope,
                connectedByAdminUserId = "admin-user-id-0000000001",
                connectedAt = fixedNow,
            )
        every { stateStore.consume("admin-user-id-0000000001", "state") } returns true
        every { googleClient.exchangeCodeForTokens("auth-code", "https://backoffice/callback") } returns
            GoogleTokenExchangeResponse(
                accessToken = "access-token",
                refreshToken = "refresh-token",
                scope = grantedScope,
            )
        every { googleClient.fetchUserInfo("access-token") } returns
            GoogleUserInfoResponse(
                id = "google-user-id",
                email = "central-google@example.com",
            )
        every { encryptor.encrypt("refresh-token") } returns "encrypted-refresh-token"
        every {
            connectCentralGoogleAccountLockedUseCase.execute(
                provider = CentralGoogleAccount.PROVIDER_GOOGLE,
                googleEmail = "central-google@example.com",
                encryptedRefreshToken = "encrypted-refresh-token",
                scope = grantedScope,
                adminUserId = "admin-user-id-0000000001",
            )
        } returns account

        val response =
            useCase.execute(
                provider = CentralGoogleAccount.PROVIDER_GOOGLE,
                adminUserId = "admin-user-id-0000000001",
                request = request,
            )

        assertAll(
            { assertEquals(true, response.connected) },
            { assertEquals("central-google@example.com", response.googleEmail) },
            { assertEquals("central-google@example.com", response.allowedEmail) },
            { assertEquals("admin-user-id-0000000001", response.connectedByAdminUserId) },
            { assertEquals(fixedNow, response.connectedAt) },
            { assertEquals(grantedScope, response.scope) },
        )
        verify {
            connectCentralGoogleAccountLockedUseCase.execute(
                provider = CentralGoogleAccount.PROVIDER_GOOGLE,
                googleEmail = "central-google@example.com",
                encryptedRefreshToken = "encrypted-refresh-token",
                scope = grantedScope,
                adminUserId = "admin-user-id-0000000001",
            )
        }
    }

    @Test
    fun `중앙 Google Calendar 연동이 비활성화되어 있으면 state를 소비하지 않는다`() {
        val disabledUseCase =
            ConnectCentralGoogleLockedUseCase(
                googleClient = googleClient,
                connectCentralGoogleAccountLockedUseCase = connectCentralGoogleAccountLockedUseCase,
                encryptor = encryptor,
                stateStore = stateStore,
                properties = GoogleCentralCalendarProperties(),
            )
        val request = ConnectCentralGoogleRequest(code = "auth-code", redirectUri = "https://backoffice/callback", state = "state")

        assertThrows<GoogleCalendarCentralDisabledException> {
            disabledUseCase.execute(
                provider = CentralGoogleAccount.PROVIDER_GOOGLE,
                adminUserId = "admin-user-id-0000000001",
                request = request,
            )
        }

        verify(exactly = 0) { stateStore.consume(any(), any()) }
        verify(exactly = 0) { googleClient.exchangeCodeForTokens(any(), any()) }
    }

    @Test
    fun `허용 이메일과 다른 Google 계정이면 저장하지 않는다`() {
        val request = ConnectCentralGoogleRequest(code = "auth-code", redirectUri = "https://backoffice/callback", state = "state")
        every { stateStore.consume("admin-user-id-0000000001", "state") } returns true
        every { googleClient.exchangeCodeForTokens("auth-code", "https://backoffice/callback") } returns
            GoogleTokenExchangeResponse(
                accessToken = "access-token",
                refreshToken = "refresh-token",
                scope = grantedScope,
            )
        every { googleClient.fetchUserInfo("access-token") } returns
            GoogleUserInfoResponse(
                id = "google-user-id",
                email = "other@example.com",
            )

        assertThrows<CentralGoogleAccountEmailNotAllowedException> {
            useCase.execute(
                provider = CentralGoogleAccount.PROVIDER_GOOGLE,
                adminUserId = "admin-user-id-0000000001",
                request = request,
            )
        }

        verify(exactly = 0) { encryptor.encrypt(any()) }
        verify(exactly = 0) { connectCentralGoogleAccountLockedUseCase.execute(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `state가 유효하지 않으면 Google token exchange를 호출하지 않는다`() {
        val request = ConnectCentralGoogleRequest(code = "auth-code", redirectUri = "https://backoffice/callback", state = "state")
        every { stateStore.consume("admin-user-id-0000000001", "state") } returns false

        assertThrows<GoogleOAuthStateInvalidException> {
            useCase.execute(
                provider = CentralGoogleAccount.PROVIDER_GOOGLE,
                adminUserId = "admin-user-id-0000000001",
                request = request,
            )
        }

        verify(exactly = 0) { googleClient.exchangeCodeForTokens(any(), any()) }
    }

    @Test
    fun `Google Meet 녹화 파일 조회 scope가 없으면 실패한다`() {
        val request = ConnectCentralGoogleRequest(code = "auth-code", redirectUri = "https://backoffice/callback", state = "state")
        every { stateStore.consume("admin-user-id-0000000001", "state") } returns true
        every { googleClient.exchangeCodeForTokens("auth-code", "https://backoffice/callback") } returns
            GoogleTokenExchangeResponse(
                accessToken = "access-token",
                refreshToken = "refresh-token",
                scope = "openid https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/calendar.events",
            )

        assertThrows<GoogleDriveScopeMissingException> {
            useCase.execute(
                provider = CentralGoogleAccount.PROVIDER_GOOGLE,
                adminUserId = "admin-user-id-0000000001",
                request = request,
            )
        }

        verify(exactly = 0) { googleClient.fetchUserInfo(any()) }
        verify(exactly = 0) { connectCentralGoogleAccountLockedUseCase.execute(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `connect와 disconnect 경합을 막기 위해 provider 기반 분산 락이 전체 연결 흐름에 적용된다`() {
        val method =
            ConnectCentralGoogleLockedUseCase::class.java.getDeclaredMethod(
                "execute",
                String::class.java,
                String::class.java,
                ConnectCentralGoogleRequest::class.java,
            )

        val lock = method.getAnnotation(DistributedLock::class.java)

        assertAll(
            { assertNotNull(lock) },
            { assertEquals("central-google-account", lock.prefix) },
            { assertEquals(30L, lock.waitTime) },
            { assertTrue(method.parameters[0].isAnnotationPresent(LockKey::class.java)) },
        )
    }
}
