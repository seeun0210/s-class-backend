package com.sclass.backoffice.teacher.dto

import com.sclass.domain.domains.teacher.domain.MajorCategory
import com.sclass.domain.domains.teacher.domain.Teacher
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.UserRole
import com.sclass.domain.domains.user.domain.UserRoleState
import java.time.LocalDateTime

data class TeacherListResponse(
    val id: String,
    val userId: String,
    val name: String,
    val email: String,
    val university: String?,
    val major: String?,
    val majorCategory: MajorCategory?,
    val roles: List<TeacherRoleInfo>,
    val submittedAt: LocalDateTime?,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(
            teacher: Teacher,
            roles: List<UserRole>,
        ): TeacherListResponse =
            TeacherListResponse(
                id = teacher.id,
                userId = teacher.user.id,
                name = teacher.user.name,
                email = teacher.user.email,
                university = teacher.education?.university,
                major = teacher.education?.major,
                majorCategory = teacher.education?.majorCategory,
                roles = roles.map { TeacherRoleInfo.from(it) },
                submittedAt = teacher.verification?.submittedAt,
                createdAt = teacher.createdAt,
            )
    }
}

data class TeacherRoleInfo(
    val userRoleId: String,
    val platform: Platform,
    val state: UserRoleState,
) {
    companion object {
        fun from(userRole: UserRole): TeacherRoleInfo =
            TeacherRoleInfo(
                userRoleId = userRole.id,
                platform = userRole.platform,
                state = userRole.state,
            )
    }
}
