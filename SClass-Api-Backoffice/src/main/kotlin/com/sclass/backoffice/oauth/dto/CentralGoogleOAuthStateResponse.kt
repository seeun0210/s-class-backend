package com.sclass.backoffice.oauth.dto

data class CentralGoogleOAuthStateResponse(
    val state: String,
    val expiresInSeconds: Long,
)
