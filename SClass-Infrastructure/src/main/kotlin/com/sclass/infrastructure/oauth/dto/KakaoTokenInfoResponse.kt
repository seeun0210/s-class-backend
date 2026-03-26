package com.sclass.infrastructure.oauth.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class KakaoTokenInfoResponse(
    @param:JsonProperty("app_id")
    val appId: Long = 0,
)
