package com.sclass.infrastructure.oauth.client

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.sclass.common.exception.OAuthTokenValidationFailedException
import com.sclass.infrastructure.oauth.config.OAuthProperties
import com.sclass.infrastructure.oauth.dto.OAuthUserInfo
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class GoogleOAuthClient(
    oAuthProperties: OAuthProperties,
) : OAuthClient {
    private val log = LoggerFactory.getLogger(javaClass)

    override val provider = "GOOGLE"

    private val verifier: GoogleIdTokenVerifier =
        GoogleIdTokenVerifier
            .Builder(
                NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
            ).setAudience(
                listOf(
                    oAuthProperties.providers["google"]?.clientId
                        ?: throw IllegalStateException("Google OAuth client-id가 설정되지 않았습니다"),
                ),
            ).build()

    override fun fetchUserInfo(accessToken: String): OAuthUserInfo {
        val idToken =
            try {
                verifier.verify(accessToken)
            } catch (e: Exception) {
                log.warn("Google ID token verification failed", e)
                throw OAuthTokenValidationFailedException()
            } ?: throw OAuthTokenValidationFailedException()

        val payload = idToken.payload
        return OAuthUserInfo(
            id = payload.subject,
            email = payload.email,
            name = payload["name"] as? String ?: "",
        )
    }
}
