package com.sclass.infrastructure.oauth.client

import com.sclass.infrastructure.calendar.GoogleCentralCalendarProperties
import com.sclass.infrastructure.oauth.dto.GoogleTokenExchangeResponse
import com.sclass.infrastructure.oauth.dto.GoogleUserInfoResponse
import org.springframework.stereotype.Component

@Component
class CentralGoogleAuthorizationCodeClient(
    private val properties: GoogleCentralCalendarProperties,
    private val tokenClient: GoogleOAuthTokenClient,
) {
    fun exchangeCodeForTokens(
        code: String,
        redirectUri: String,
    ): GoogleTokenExchangeResponse =
        tokenClient.exchangeCodeForTokens(
            code = code,
            redirectUri = redirectUri,
            clientId = properties.requireClientId(),
            clientSecret = properties.requireClientSecret(),
        )

    fun refreshAccessToken(refreshToken: String): String =
        tokenClient.refreshAccessToken(
            refreshToken = refreshToken,
            clientId = properties.requireClientId(),
            clientSecret = properties.requireClientSecret(),
        )

    fun revokeRefreshToken(refreshToken: String) {
        tokenClient.revokeRefreshToken(refreshToken)
    }

    fun fetchUserInfo(accessToken: String): GoogleUserInfoResponse = tokenClient.fetchUserInfo(accessToken)
}
