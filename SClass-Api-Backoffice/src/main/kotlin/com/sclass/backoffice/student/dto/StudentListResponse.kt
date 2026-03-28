package com.sclass.backoffice.student.dto

import com.sclass.domain.domains.student.domain.Student
import com.sclass.domain.domains.user.domain.Grade
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.UserRole
import com.sclass.domain.domains.user.domain.UserRoleState
import java.time.LocalDateTime

data class StudentListResponse(
    val id: String,
    val userId: String,
    val name: String,
    val email: String,
    val grade: Grade?,
    val school: String?,
    val roles: List<StudentRoleInfo>,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(
            student: Student,
            roles: List<UserRole>,
        ): StudentListResponse =
            StudentListResponse(
                id = student.id,
                userId = student.user.id,
                name = student.user.name,
                email = student.user.email,
                grade = student.grade,
                school = student.school,
                roles = roles.map { StudentRoleInfo.from(it) },
                createdAt = student.createdAt,
            )
    }
}

data class StudentRoleInfo(
    val userRoleId: String,
    val platform: Platform,
    val state: UserRoleState,
) {
    companion object {
        fun from(userRole: UserRole): StudentRoleInfo =
            StudentRoleInfo(
                userRoleId = userRole.id,
                platform = userRole.platform,
                state = userRole.state,
            )
    }
}
