package com.sclass.infrastructure.oauth.client

import com.sclass.infrastructure.oauth.dto.GoogleUserInfoResponse
import com.sclass.infrastructure.oauth.dto.OAuthUserInfo
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class GoogleOAuthClient(
    webClient: WebClient,
) : AbstractOAuthClient(webClient) {
    override val provider = "GOOGLE"
    override val userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo"

    override fun fetchUserInfo(accessToken: String): OAuthUserInfo {
        val response = requestUserInfo(accessToken, GoogleUserInfoResponse::class.java)
        return OAuthUserInfo(
            id = response.id,
            email = response.email,
            name = response.name,
        )
    }
}
