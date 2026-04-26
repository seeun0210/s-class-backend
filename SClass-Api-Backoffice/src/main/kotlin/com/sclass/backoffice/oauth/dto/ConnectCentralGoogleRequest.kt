package com.sclass.backoffice.oauth.dto

import jakarta.validation.constraints.NotBlank

data class ConnectCentralGoogleRequest(
    @field:NotBlank
    val code: String,
    @field:NotBlank
    val redirectUri: String,
    @field:NotBlank
    val state: String,
)
