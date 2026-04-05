package com.sclass.infrastructure.nicepay.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class NicePayWebhookPayload(
    @param:JsonProperty("tid") val tid: String,
    @param:JsonProperty("orderId") val orderId: String,
    @param:JsonProperty("amount") val amount: Int,
    @param:JsonProperty("ediDate") val ediDate: String,
    @param:JsonProperty("signature") val signature: String,
    @param:JsonProperty("resultCode") val resultCode: String,
    @param:JsonProperty("resultMsg") val resultMsg: String,
    @param:JsonProperty("cancelledTid") val cancelledTid: String? = null,
)
