package com.sclass.infrastructure.oauth.client

import com.sclass.common.exception.OAuthTokenAudienceMismatchException
import com.sclass.common.exception.OAuthTokenValidationFailedException
import com.sclass.infrastructure.oauth.config.OAuthProperties
import com.sclass.infrastructure.oauth.dto.KakaoTokenInfoResponse
import com.sclass.infrastructure.oauth.dto.KakaoUserInfoResponse
import com.sclass.infrastructure.oauth.dto.OAuthUserInfo
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException

@Component
class KakaoOAuthClient(
    @Qualifier("oAuthWebClient") private val webClient: WebClient,
    private val oAuthProperties: OAuthProperties,
) : OAuthClient {
    override val provider = "KAKAO"

    private val userInfoUrl = "https://kapi.kakao.com/v2/user/me"

    private fun providerConfig() =
        oAuthProperties.providers["kakao"]
            ?: throw IllegalStateException("OAuth provider config not found: KAKAO")

    private fun validateToken(accessToken: String) {
        val tokenInfo =
            try {
                webClient
                    .get()
                    .uri("https://kapi.kakao.com/v1/user/access_token_info")
                    .headers { it.setBearerAuth(accessToken) }
                    .retrieve()
                    .bodyToMono(KakaoTokenInfoResponse::class.java)
                    .block() ?: throw OAuthTokenValidationFailedException()
            } catch (e: WebClientResponseException) {
                throw OAuthTokenValidationFailedException()
            }
        if (tokenInfo.appId != providerConfig().appId) {
            throw OAuthTokenAudienceMismatchException()
        }
    }

    override fun fetchUserInfo(accessToken: String): OAuthUserInfo {
        validateToken(accessToken)

        val response =
            try {
                webClient
                    .get()
                    .uri(userInfoUrl)
                    .headers { it.setBearerAuth(accessToken) }
                    .retrieve()
                    .bodyToMono(KakaoUserInfoResponse::class.java)
                    .block() ?: throw IllegalStateException("OAuth 사용자 정보 응답이 비어있습니다.")
            } catch (e: WebClientResponseException) {
                throw IllegalStateException("OAuth 사용자 정보 조회 실패", e)
            }
        return OAuthUserInfo(
            id = response.id.toString(),
            email = response.kakaoAccount.email,
            name = response.kakaoAccount.profile.nickname,
        )
    }
}
