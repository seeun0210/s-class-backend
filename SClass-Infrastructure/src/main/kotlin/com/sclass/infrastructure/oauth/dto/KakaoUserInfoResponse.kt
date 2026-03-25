package com.sclass.infrastructure.oauth.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class KakaoUserInfoResponse(
    val id: Long,

    @get:JsonProperty("kakao_account")
    val kakaoAccount: KakaoAccount,
)

data class KakaoAccount(
    val email: String,
    val profile: KakaoProfile,
)

data class KakaoProfile(
    val nickname: String,
)
