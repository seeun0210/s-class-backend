package com.sclass.lms.auth.dto

import com.sclass.domain.domains.user.domain.AuthProvider
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.Role
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class OAuthLoginRequest(
    @field:NotNull
    val provider: AuthProvider,

    @field:NotBlank
    val accessToken: String,

    @field:NotNull
    val role: Role,

    @field:NotNull
    val platform: Platform,
)
