package com.sclass.backoffice.student.dto

import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.UserRoleState
import jakarta.validation.constraints.NotNull

data class UpdateStudentStateRequest(
    @field:NotNull
    val state: UserRoleState,

    @field:NotNull
    val platform: Platform,
)
