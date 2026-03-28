package com.sclass.domain.domains.teacherassignment.dto

import com.sclass.domain.domains.user.domain.Platform

data class TeacherAssignmentSearchCondition(
    val platform: Platform? = null,
    val organizationId: Long? = null,
    val teacherName: String? = null,
    val studentName: String? = null,
)
