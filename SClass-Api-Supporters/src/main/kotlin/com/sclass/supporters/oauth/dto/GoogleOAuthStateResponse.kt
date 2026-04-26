package com.sclass.supporters.oauth.dto

data class GoogleOAuthStateResponse(
    val state: String,
    val expiresInSeconds: Long,
)
