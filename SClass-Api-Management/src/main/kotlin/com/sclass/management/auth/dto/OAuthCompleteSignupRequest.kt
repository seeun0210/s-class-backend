package com.sclass.management.auth.dto

import jakarta.annotation.Nullable
import jakarta.validation.constraints.NotBlank

data class OAuthCompleteSignupRequest(
    @field:NotBlank
    val signupToken: String,

    @field:NotBlank
    val phoneNumber: String,

    @field:Nullable
    val profileImageUrl: String? = null,
)
