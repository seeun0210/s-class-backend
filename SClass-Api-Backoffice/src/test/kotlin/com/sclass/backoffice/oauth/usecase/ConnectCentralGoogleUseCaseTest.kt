package com.sclass.backoffice.oauth.usecase

import com.sclass.backoffice.oauth.dto.ConnectCentralGoogleRequest
import com.sclass.common.exception.GoogleDriveScopeMissingException
import com.sclass.common.exception.GoogleOAuthStateInvalidException
import com.sclass.common.jwt.AesTokenEncryptor
import com.sclass.domain.domains.oauth.adaptor.CentralGoogleAccountAdaptor
import com.sclass.domain.domains.oauth.domain.CentralGoogleAccount
import com.sclass.domain.domains.oauth.exception.CentralGoogleAccountEmailNotAllowedException
import com.sclass.infrastructure.calendar.GoogleCentralCalendarProperties
import com.sclass.infrastructure.oauth.client.CentralGoogleAuthorizationCodeClient
import com.sclass.infrastructure.oauth.dto.GoogleTokenExchangeResponse
import com.sclass.infrastructure.oauth.dto.GoogleUserInfoResponse
import com.sclass.infrastructure.oauth.state.GoogleOAuthStateStore
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId

class ConnectCentralGoogleUseCaseTest {
    private val grantedScope = CentralGoogleOAuthScopes.authorizationScopes.joinToString(" ")

    private val googleClient: CentralGoogleAuthorizationCodeClient = mockk()
    private val centralGoogleAccountAdaptor: CentralGoogleAccountAdaptor = mockk()
    private val encryptor: AesTokenEncryptor = mockk()
    private val stateStore: GoogleOAuthStateStore = mockk()
    private val fixedNow = LocalDateTime.of(2026, 4, 26, 14, 0)
    private val clock =
        Clock.fixed(
            fixedNow.atZone(ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault(),
        )
    private val properties =
        GoogleCentralCalendarProperties().apply {
            allowedEmail = "central-google@example.com"
        }
    private val useCase =
        ConnectCentralGoogleUseCase(
            googleClient = googleClient,
            centralGoogleAccountAdaptor = centralGoogleAccountAdaptor,
            encryptor = encryptor,
            stateStore = stateStore,
            properties = properties,
            clock = clock,
        )

    @Test
    fun `허용된 중앙 Google 계정이면 refresh token을 암호화 저장한다`() {
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
                email = "central-google@example.com",
            )
        every { encryptor.encrypt("refresh-token") } returns "encrypted-refresh-token"
        every { centralGoogleAccountAdaptor.findGoogleOrNull() } returns null
        every { centralGoogleAccountAdaptor.save(any()) } answers { firstArg() }

        val response = useCase.execute("admin-user-id-0000000001", request)

        assertAll(
            { assertEquals(true, response.connected) },
            { assertEquals("central-google@example.com", response.googleEmail) },
            { assertEquals("central-google@example.com", response.allowedEmail) },
            { assertEquals("admin-user-id-0000000001", response.connectedByAdminUserId) },
            { assertEquals(fixedNow, response.connectedAt) },
            { assertEquals(grantedScope, response.scope) },
        )
        verify {
            centralGoogleAccountAdaptor.save(
                match<CentralGoogleAccount> {
                    it.googleEmail == "central-google@example.com" &&
                        it.encryptedRefreshToken == "encrypted-refresh-token" &&
                        it.connectedByAdminUserId == "admin-user-id-0000000001" &&
                        it.connectedAt == fixedNow
                },
            )
        }
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
            useCase.execute("admin-user-id-0000000001", request)
        }

        verify(exactly = 0) { encryptor.encrypt(any()) }
        verify(exactly = 0) { centralGoogleAccountAdaptor.save(any()) }
    }

    @Test
    fun `state가 유효하지 않으면 Google token exchange를 호출하지 않는다`() {
        val request = ConnectCentralGoogleRequest(code = "auth-code", redirectUri = "https://backoffice/callback", state = "state")
        every { stateStore.consume("admin-user-id-0000000001", "state") } returns false

        assertThrows<GoogleOAuthStateInvalidException> {
            useCase.execute("admin-user-id-0000000001", request)
        }

        verify(exactly = 0) { googleClient.exchangeCodeForTokens(any(), any()) }
    }

    @Test
    fun `기존 중앙 계정이 있으면 재연결 정보로 갱신한다`() {
        val existing =
            CentralGoogleAccount(
                googleEmail = "central-google@example.com",
                encryptedRefreshToken = "old-token",
                scope = "old-scope",
                connectedByAdminUserId = "old-admin-user-id-00001",
                connectedAt = fixedNow.minusDays(1),
            )
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
                email = "central-google@example.com",
            )
        every { encryptor.encrypt("refresh-token") } returns "new-encrypted-refresh-token"
        every { centralGoogleAccountAdaptor.findGoogleOrNull() } returns existing
        every { centralGoogleAccountAdaptor.save(existing) } returns existing

        useCase.execute("admin-user-id-0000000001", request)

        assertAll(
            { assertEquals("new-encrypted-refresh-token", existing.encryptedRefreshToken) },
            { assertEquals("admin-user-id-0000000001", existing.connectedByAdminUserId) },
            { assertEquals(fixedNow, existing.connectedAt) },
            { assertEquals(grantedScope, existing.scope) },
        )
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
            useCase.execute("admin-user-id-0000000001", request)
        }

        verify(exactly = 0) { googleClient.fetchUserInfo(any()) }
        verify(exactly = 0) { centralGoogleAccountAdaptor.save(any()) }
    }
}
