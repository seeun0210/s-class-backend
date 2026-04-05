package com.sclass.infrastructure.nicepay.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class NicePayApproveResponse(
    @param:JsonProperty("resultCode") val resultCode: String,
    @param:JsonProperty("resultMsg") val resultMsg: String,
    @param:JsonProperty("tid") val tid: String? = null,
    @param:JsonProperty("orderId") val orderId: String? = null,
    @param:JsonProperty("amount") val amount: Int? = null,
)
