package com.sclass.supporters.auth.dto

import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.Role
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class OAuthLoginRequest(
    @field:NotBlank
    val provider: String,

    @field:NotBlank
    val accessToken: String,

    @field:NotNull
    val role: Role,

    @field:NotNull
    val platform: Platform,
)
