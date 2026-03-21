package com.sclass.infrastructure.oauth.client

import com.sclass.infrastructure.oauth.dto.KakaoUserInfoResponse
import com.sclass.infrastructure.oauth.dto.OAuthUserInfo
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class KakaoOAuthClient(
    restTemplate: RestTemplate,
) : AbstractOAuthClient(restTemplate) {
    override val provider = "KAKAO"
    override val userInfoUrl = "https://kapi.kakao.com/v2/user/me"

    override fun fetchUserInfo(accessToken: String): OAuthUserInfo {
        val response = requestUserInfo(accessToken, KakaoUserInfoResponse::class.java)
        return OAuthUserInfo(
            id = response.id.toString(),
            email = response.kakaoAccount.email,
            name = response.kakaoAccount.profile.nickname,
        )
    }
}
