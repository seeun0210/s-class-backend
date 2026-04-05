package com.sclass.infrastructure.nicepay.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class NicePayCancelResponse(
    @param:JsonProperty("resultCode") val resultCode: String,
    @param:JsonProperty("resultMsg") val resultMsg: String,
    @param:JsonProperty("tid") val tid: String? = null,
    @param:JsonProperty("cancelAmt") val cancelAmt: Int? = null,
)
