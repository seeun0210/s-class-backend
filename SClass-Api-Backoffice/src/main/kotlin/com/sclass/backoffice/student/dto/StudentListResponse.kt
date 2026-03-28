package com.sclass.backoffice.student.dto

import com.sclass.domain.domains.student.domain.Student
import com.sclass.domain.domains.user.domain.Grade
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.UserRoleState
import java.time.LocalDateTime

data class StudentListResponse(
    val id: String,
    val userId: String,
    val name: String,
    val email: String,
    val platform: Platform,
    val grade: Grade?,
    val school: String?,
    val state: UserRoleState,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(
            student: Student,
            platform: Platform,
            state: UserRoleState,
        ): StudentListResponse =
            StudentListResponse(
                id = student.id,
                userId = student.user.id,
                name = student.user.name,
                email = student.user.email,
                platform = platform,
                grade = student.grade,
                school = student.school,
                state = state,
                createdAt = student.createdAt,
            )
    }
}
