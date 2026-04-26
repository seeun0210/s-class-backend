package com.sclass.infrastructure.oauth.client

import com.sclass.infrastructure.calendar.GoogleCentralCalendarProperties
import com.sclass.infrastructure.oauth.dto.GoogleTokenExchangeResponse
import com.sclass.infrastructure.oauth.dto.GoogleUserInfoResponse
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CentralGoogleAuthorizationCodeClientTest {
    private val properties =
        GoogleCentralCalendarProperties().apply {
            clientId = "central-client-id"
            clientSecret = "central-client-secret"
        }
    private val tokenClient: GoogleOAuthTokenClient = mockk()
    private val client = CentralGoogleAuthorizationCodeClient(properties, tokenClient)

    @Test
    fun `central credential로 authorization code를 token으로 교환한다`() {
        val expected =
            GoogleTokenExchangeResponse(
                accessToken = "access-token",
                refreshToken = "refresh-token",
                scope =
                    "openid https://www.googleapis.com/auth/userinfo.email " +
                        "https://www.googleapis.com/auth/calendar.events " +
                        "https://www.googleapis.com/auth/drive.meet.readonly",
            )
        every {
            tokenClient.exchangeCodeForTokens(
                code = "auth-code",
                redirectUri = "https://backoffice/callback",
                clientId = "central-client-id",
                clientSecret = "central-client-secret",
            )
        } returns expected

        val result = client.exchangeCodeForTokens("auth-code", "https://backoffice/callback")

        assertEquals(expected, result)
    }

    @Test
    fun `central credential로 refresh token을 access token으로 갱신한다`() {
        every {
            tokenClient.refreshAccessToken(
                refreshToken = "refresh-token",
                clientId = "central-client-id",
                clientSecret = "central-client-secret",
            )
        } returns "access-token"

        val result = client.refreshAccessToken("refresh-token")

        assertEquals("access-token", result)
    }

    @Test
    fun `revoke와 user info 조회는 token client로 위임한다`() {
        every { tokenClient.revokeRefreshToken("refresh-token") } returns Unit
        every { tokenClient.fetchUserInfo("access-token") } returns
            GoogleUserInfoResponse(
                id = "google-user-id",
                email = "central-google@example.com",
            )

        client.revokeRefreshToken("refresh-token")
        val userInfo = client.fetchUserInfo("access-token")

        assertEquals("central-google@example.com", userInfo.email)
        verify { tokenClient.revokeRefreshToken("refresh-token") }
        verify { tokenClient.fetchUserInfo("access-token") }
    }
}
