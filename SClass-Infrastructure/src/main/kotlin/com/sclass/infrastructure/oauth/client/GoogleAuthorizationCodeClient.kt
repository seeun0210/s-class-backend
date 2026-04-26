package com.sclass.infrastructure.oauth.client

import com.sclass.common.exception.GoogleOAuthProviderUnavailableException
import com.sclass.common.exception.GoogleTokenExchangeFailedException
import com.sclass.common.exception.GoogleTokenRefreshFailedException
import com.sclass.infrastructure.oauth.config.OAuthProperties
import com.sclass.infrastructure.oauth.dto.GoogleTokenExchangeResponse
import com.sclass.infrastructure.oauth.dto.GoogleUserInfoResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException

@Component
class GoogleAuthorizationCodeClient(
    private val oAuthProperties: OAuthProperties,
    @param:Qualifier("oAuthWebClient") private val webClient: WebClient,
) {
    private val log = LoggerFactory.getLogger(javaClass)

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
    ): GoogleTokenExchangeResponse {
        val params =
            LinkedMultiValueMap<String, String>().apply {
                add("code", code)
                add("client_id", clientId)
                add("client_secret", clientSecret)
                add("redirect_uri", redirectUri)
                add("grant_type", "authorization_code")
            }

        val response =
            try {
                webClient
                    .post()
                    .uri("https://oauth2.googleapis.com/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(params))
                    .retrieve()
                    .bodyToMono(GoogleTokenExchangeResponse::class.java)
                    .block()
            } catch (e: WebClientResponseException) {
                log.warn("Google token exchange failed: ${e.responseBodyAsString}", e)
                if (e.isRetryableUpstreamFailure()) {
                    throw GoogleOAuthProviderUnavailableException()
                }
                throw GoogleTokenExchangeFailedException()
            } catch (e: Exception) {
                log.warn("Google token exchange failed", e)
                throw GoogleOAuthProviderUnavailableException()
            }

        return response ?: throw GoogleOAuthProviderUnavailableException()
    }

    fun refreshAccessToken(refreshToken: String): String {
        val params =
            LinkedMultiValueMap<String, String>().apply {
                add("client_id", clientId)
                add("client_secret", clientSecret)
                add("refresh_token", refreshToken)
                add("grant_type", "refresh_token")
            }

        val response =
            try {
                webClient
                    .post()
                    .uri("https://oauth2.googleapis.com/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(params))
                    .retrieve()
                    .bodyToMono(GoogleTokenExchangeResponse::class.java)
                    .block()
            } catch (e: WebClientResponseException) {
                log.warn("Google token refresh failed: ${e.responseBodyAsString}", e)
                if (e.isRetryableUpstreamFailure()) {
                    throw GoogleOAuthProviderUnavailableException()
                }
                throw GoogleTokenRefreshFailedException()
            } catch (e: Exception) {
                log.warn("Google token refresh failed", e)
                throw GoogleOAuthProviderUnavailableException()
            }

        return response?.accessToken ?: throw GoogleOAuthProviderUnavailableException()
    }

    fun revokeRefreshToken(refreshToken: String) {
        val params =
            LinkedMultiValueMap<String, String>().apply {
                add("token", refreshToken)
            }

        try {
            webClient
                .post()
                .uri("https://oauth2.googleapis.com/revoke")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(params))
                .retrieve()
                .toBodilessEntity()
                .block()
        } catch (e: Exception) {
            log.warn("Google token revoke failed (best effort, ignoring)", e)
        }
    }

    fun fetchUserInfo(accessToken: String): GoogleUserInfoResponse {
        val userInfo =
            try {
                webClient
                    .get()
                    .uri("https://www.googleapis.com/oauth2/v2/userinfo")
                    .header("Authorization", "Bearer $accessToken")
                    .retrieve()
                    .bodyToMono(GoogleUserInfoResponse::class.java)
                    .block()
            } catch (e: WebClientResponseException) {
                log.warn("Google fetch user info failed: ${e.responseBodyAsString}", e)
                if (e.isRetryableUpstreamFailure()) {
                    throw GoogleOAuthProviderUnavailableException()
                }
                throw GoogleTokenExchangeFailedException()
            } catch (e: Exception) {
                log.warn("Google fetch user info failed", e)
                throw GoogleOAuthProviderUnavailableException()
            }

        return userInfo ?: throw GoogleOAuthProviderUnavailableException()
    }

    private fun WebClientResponseException.isRetryableUpstreamFailure(): Boolean =
        statusCode.is5xxServerError || statusCode.value() == TOO_MANY_REQUESTS_STATUS

    private companion object {
        const val TOO_MANY_REQUESTS_STATUS = 429
    }
}
