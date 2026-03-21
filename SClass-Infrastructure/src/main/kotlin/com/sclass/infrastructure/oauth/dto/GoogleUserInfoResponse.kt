package com.sclass.infrastructure.oauth.dto

data class GoogleUserInfoResponse(
    val id: String,
    val email: String,
    val name: String,
)
