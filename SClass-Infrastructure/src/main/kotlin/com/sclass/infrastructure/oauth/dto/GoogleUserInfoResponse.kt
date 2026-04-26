package com.sclass.infrastructure.oauth.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class GoogleUserInfoResponse(
    val id: String,
    val email: String,
    @param:JsonProperty("verified_email") val verifiedEmail: Boolean = false,
    val name: String? = null,
    val picture: String? = null,
)
