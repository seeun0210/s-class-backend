package com.sclass.backoffice.student.dto

import com.sclass.domain.domains.user.domain.Grade
import com.sclass.domain.domains.user.domain.Platform

data class CreateStudentResponse(
    val studentId: String,
    val userId: String,
    val email: String,
    val name: String,
    val platform: Platform,
    val phoneNumber: String,
    val grade: Grade? = null,
    val school: String? = null,
    val parentPhoneNumber: String? = null,
)
