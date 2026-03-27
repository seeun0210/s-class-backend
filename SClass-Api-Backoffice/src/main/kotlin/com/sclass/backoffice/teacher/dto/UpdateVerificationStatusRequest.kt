package com.sclass.backoffice.teacher.dto

import com.sclass.domain.domains.teacher.domain.TeacherVerificationStatus
import com.sclass.domain.domains.teacher.exception.TeacherInvalidVerificationStatusException
import com.sclass.domain.domains.teacher.exception.TeacherRejectReasonRequiredException

data class UpdateVerificationStatusRequest(
    val status: TeacherVerificationStatus,
    val reason: String? = null,
) {
    val isApproved: Boolean
        get() = status == TeacherVerificationStatus.APPROVED

    val requireReason: String
        get() = checkNotNull(reason)

    init {
        if (status != TeacherVerificationStatus.APPROVED && status != TeacherVerificationStatus.REJECTED) {
            throw TeacherInvalidVerificationStatusException()
        }

        if (status == TeacherVerificationStatus.REJECTED && reason == null) {
            throw TeacherRejectReasonRequiredException()
        }
    }
}
