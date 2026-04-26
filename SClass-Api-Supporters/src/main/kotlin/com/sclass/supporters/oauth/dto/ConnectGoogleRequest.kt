package com.sclass.supporters.oauth.dto

import jakarta.validation.constraints.NotBlank

data class ConnectGoogleRequest(
    @field:NotBlank val code: String,
    @field:NotBlank val redirectUri: String,
)
