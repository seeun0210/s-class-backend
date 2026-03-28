package com.sclass.backoffice.userrole.dto

import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.Role
import com.sclass.domain.domains.user.domain.UserRole
import com.sclass.domain.domains.user.domain.UserRoleState

data class AddUserRoleResponse(
    val userRoleId: String,
    val userId: String,
    val platform: Platform,
    val role: Role,
    val state: UserRoleState,
) {
    companion object {
        fun from(userRole: UserRole): AddUserRoleResponse =
            AddUserRoleResponse(
                userRoleId = userRole.id,
                userId = userRole.userId,
                platform = userRole.platform,
                role = userRole.role,
                state = userRole.state,
            )
    }
}
