package com.sclass.backoffice.teacher.dto

import com.sclass.domain.domains.teacher.exception.TeacherInvalidVerificationStatusException
import com.sclass.domain.domains.teacher.exception.TeacherRejectReasonRequiredException
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.UserRoleState

data class UpdateTeacherStateRequest(
    val state: UserRoleState,
    val platform: Platform,
    val reason: String? = null,
) {
    val isApproved: Boolean
        get() = state == UserRoleState.APPROVED

    val requireReason: String
        get() = checkNotNull(reason)

    init {
        if (state != UserRoleState.APPROVED && state != UserRoleState.REJECTED) {
            throw TeacherInvalidVerificationStatusException()
        }

        if (state == UserRoleState.REJECTED && reason == null) {
            throw TeacherRejectReasonRequiredException()
        }
    }
}
