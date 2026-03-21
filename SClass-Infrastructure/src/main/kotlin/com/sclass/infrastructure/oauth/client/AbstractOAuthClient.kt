package com.sclass.infrastructure.oauth.client

import com.sclass.infrastructure.oauth.dto.OAuthUserInfo
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate

abstract class AbstractOAuthClient(
    private val restTemplate: RestTemplate,
) : OAuthClient {
    protected abstract val userInfoUrl: String

    protected fun <T : Any> requestUserInfo(
        accessToken: String,
        responseType: Class<T>,
    ): T {
        val headers = HttpHeaders()
        headers.setBearerAuth(accessToken)
        val entity = HttpEntity<Void>(headers)

        try {
            val response =
                restTemplate.exchange(userInfoUrl, HttpMethod.GET, entity, responseType)

            return response.body ?: throw RestClientException("OAuth 사용자 정보 응답이 비어있습니다.")
        } catch (e: RestClientException) {
            throw RestClientException("OAuth 사용자 정보 조회 실패", e)
        }
    }

    abstract override fun fetchUserInfo(accessToken: String): OAuthUserInfo
}
