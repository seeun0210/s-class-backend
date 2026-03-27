package com.sclass.backoffice.teacher.dto

import com.sclass.domain.domains.teacher.domain.MajorCategory
import com.sclass.domain.domains.teacher.domain.Teacher
import com.sclass.domain.domains.teacher.domain.TeacherVerificationStatus
import com.sclass.domain.domains.user.domain.Platform
import java.time.LocalDateTime

data class TeacherListResponse(
    val id: String,
    val name: String,
    val email: String,
    val platform: Platform,
    val university: String?,
    val major: String?,
    val majorCategory: MajorCategory?,
    val verificationStatus: TeacherVerificationStatus,
    val submittedAt: LocalDateTime?,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(
            teacher: Teacher,
            platform: Platform,
        ): TeacherListResponse =
            TeacherListResponse(
                id = teacher.id,
                name = teacher.user.name,
                email = teacher.user.email,
                platform = platform,
                university = teacher.education?.university,
                major = teacher.education?.major,
                majorCategory = teacher.education?.majorCategory,
                verificationStatus = teacher.verification?.verificationStatus ?: TeacherVerificationStatus.DRAFT,
                submittedAt = teacher.verification?.submittedAt,
                createdAt = teacher.createdAt,
            )
    }
}
