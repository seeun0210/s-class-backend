package com.sclass.infrastructure.oauth.client

import com.sclass.infrastructure.oauth.dto.OAuthUserInfo
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException

abstract class AbstractOAuthClient(
    private val webClient: WebClient,
) : OAuthClient {
    protected abstract val userInfoUrl: String

    protected fun <T : Any> requestUserInfo(
        accessToken: String,
        responseType: Class<T>,
    ): T {
        try {
            return webClient
                .get()
                .uri(userInfoUrl)
                .headers { it.setBearerAuth(accessToken) }
                .retrieve()
                .bodyToMono(responseType)
                .block() ?: throw IllegalStateException("OAuth 사용자 정보 응답이 비어있습니다.")
        } catch (e: WebClientResponseException) {
            throw IllegalStateException("OAuth 사용자 정보 조회 실패", e)
        }
    }

    abstract override fun fetchUserInfo(accessToken: String): OAuthUserInfo
}
