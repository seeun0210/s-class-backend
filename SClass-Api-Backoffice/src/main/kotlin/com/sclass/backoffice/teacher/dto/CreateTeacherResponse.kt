package com.sclass.backoffice.teacher.dto

import com.sclass.domain.domains.teacher.domain.MajorCategory
import com.sclass.domain.domains.user.domain.Platform

data class CreateTeacherResponse(
    val teacherId: String,
    val userId: String,
    val email: String,
    val name: String,
    val platform: Platform,
    val university: String? = null,
    val major: String? = null,
    val majorCategory: MajorCategory? = null,
)
