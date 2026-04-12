package com.sclass.backoffice.userrole.dto

import com.sclass.domain.domains.user.domain.UserRoleState
import com.sclass.domain.domains.user.exception.InvalidStateRequestException
import com.sclass.domain.domains.user.exception.RejectReasonRequiredException

data class UpdateUserRoleStateRequest(
    val state: UserRoleState,
    val reason: String? = null,
) {
    init {
        if (state != UserRoleState.ACTIVE && state != UserRoleState.REJECTED) {
            throw InvalidStateRequestException()
        }

        if (state == UserRoleState.REJECTED && reason == null) {
            throw RejectReasonRequiredException()
        }
    }
}
