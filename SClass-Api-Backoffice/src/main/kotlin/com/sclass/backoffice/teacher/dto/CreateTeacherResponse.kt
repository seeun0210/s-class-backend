package com.sclass.backoffice.teacher.dto

import com.sclass.domain.domains.user.domain.Platform

data class CreateTeacherResponse(
    val teacherId: String,
    val userId: String,
    val email: String,
    val name: String,
    val platform: Platform,
)
