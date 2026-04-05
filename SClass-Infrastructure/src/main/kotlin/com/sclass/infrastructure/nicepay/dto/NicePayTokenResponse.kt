package com.sclass.infrastructure.nicepay.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class NicePayTokenResponse(
    @param:JsonProperty("resultCode") val resultCode: String,
    @param:JsonProperty("resultMsg") val resultMsg: String,
    @param:JsonProperty("accessToken") val accessToken: String? = null,
)
