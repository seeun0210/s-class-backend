package com.sclass.backoffice.student.dto

import com.sclass.domain.domains.user.domain.Grade

data class UpdateStudentProfileRequest(
    val grade: Grade?,
    val school: String?,
    val parentPhoneNumber: String?,
)
