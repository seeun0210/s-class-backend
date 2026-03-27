package com.sclass.domain.domains.teacher.dto

import com.sclass.domain.domains.teacher.domain.MajorCategory
import com.sclass.domain.domains.teacher.domain.TeacherVerificationStatus
import com.sclass.domain.domains.user.domain.Platform
import java.time.LocalDateTime

data class TeacherSearchCondition(
    val name: String? = null,
    val email: String? = null,
    val university: String? = null,
    val major: String? = null,
    val majorCategory: MajorCategory? = null,
    val verificationStatus: TeacherVerificationStatus? = null,
    val platform: Platform? = null,
    val submittedAtFrom: LocalDateTime? = null,
    val submittedAtTo: LocalDateTime? = null,
    val createdAtFrom: LocalDateTime? = null,
    val createdAtTo: LocalDateTime? = null,
)
