package com.sclass.backoffice.userrole.dto

import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.Role
import jakarta.validation.constraints.NotNull

data class AddUserRoleRequest(
    @field:NotNull
    val platform: Platform,

    @field:NotNull
    val role: Role,

    @field:NotNull
    val userId: String,
)
