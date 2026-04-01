package com.sclass.backoffice.organization.dto

import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.Role
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateOrganizationUserRequest(
    @field:NotBlank
    @field:Email
    val email: String,

    @field:NotBlank
    val name: String,

    val phoneNumber: String? = null,

    @field:NotNull
    val platform: Platform,

    @field:NotNull
    val role: Role,
)
