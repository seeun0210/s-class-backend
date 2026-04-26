package com.sclass.infrastructure.oauth.client

import com.sclass.infrastructure.oauth.config.OAuthProperties
import com.sclass.infrastructure.oauth.dto.GoogleTokenExchangeResponse
import com.sclass.infrastructure.oauth.dto.GoogleUserInfoResponse
import org.springframework.stereotype.Component

@Component
class GoogleAuthorizationCodeClient(
    private val oAuthProperties: OAuthProperties,
    private val tokenClient: GoogleOAuthTokenClient,
) {
    private val clientId: String
        get() =
            oAuthProperties.providers["google"]?.clientId
                ?: throw IllegalStateException("Google OAuth client-id가 설정되지 않았습니다")

    private val clientSecret: String
        get() =
            oAuthProperties.providers["google"]?.clientSecret
                ?: throw IllegalStateException("Google OAuth client-secret이 설정되지 않았습니다")

    fun exchangeCodeForTokens(
        code: String,
        redirectUri: String,
    ): GoogleTokenExchangeResponse =
        tokenClient.exchangeCodeForTokens(
            code = code,
            redirectUri = redirectUri,
            clientId = clientId,
            clientSecret = clientSecret,
        )

    fun refreshAccessToken(refreshToken: String): String =
        tokenClient.refreshAccessToken(
            refreshToken = refreshToken,
            clientId = clientId,
            clientSecret = clientSecret,
        )

    fun revokeRefreshToken(refreshToken: String) {
        tokenClient.revokeRefreshToken(refreshToken)
    }

    fun fetchUserInfo(accessToken: String): GoogleUserInfoResponse = tokenClient.fetchUserInfo(accessToken)
}
