package com.sclass.backoffice.student.dto

import com.sclass.domain.domains.student.domain.Student
import com.sclass.domain.domains.user.domain.Grade
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.Role
import com.sclass.domain.domains.user.domain.UserRole
import com.sclass.domain.domains.user.domain.UserRoleState
import java.time.LocalDateTime

data class StudentDetailResponse(
    val id: String,
    val name: String,
    val email: String,
    val phoneNumber: String?,
    val profileImageUrl: String?,
    val grade: Grade?,
    val school: String?,
    val parentPhoneNumber: String?,
    val roles: List<StudentRoleResponse>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun from(
            student: Student,
            roles: List<UserRole>,
        ) = StudentDetailResponse(
            id = student.id,
            name = student.user.name,
            email = student.user.email,
            phoneNumber = student.user.phoneNumber,
            profileImageUrl = student.user.profileImageUrl,
            grade = student.grade,
            school = student.school,
            parentPhoneNumber = student.parentPhoneNumber,
            roles = roles.map { StudentRoleResponse.from(it) },
            createdAt = student.createdAt,
            updatedAt = student.updatedAt,
        )
    }
}

data class StudentRoleResponse(
    val platform: Platform,
    val role: Role,
    val state: UserRoleState,
) {
    companion object {
        fun from(userRole: UserRole) =
            StudentRoleResponse(
                platform = userRole.platform,
                role = userRole.role,
                state = userRole.state,
            )
    }
}
