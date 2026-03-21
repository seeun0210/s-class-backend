package com.sclass.supporters.auth.dto

data class OAuthLoginResponse(
    val isNewUser: Boolean,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val signupToken: String? = null,
)
