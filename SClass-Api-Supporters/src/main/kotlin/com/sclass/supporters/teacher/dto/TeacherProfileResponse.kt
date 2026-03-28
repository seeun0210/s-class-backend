package com.sclass.supporters.teacher.dto

import com.sclass.domain.domains.teacher.domain.MajorCategory
import com.sclass.domain.domains.teacher.domain.Teacher
import com.sclass.domain.domains.user.domain.UserRole
import com.sclass.domain.domains.user.domain.UserRoleState
import java.time.LocalDate
import java.time.LocalDateTime

data class TeacherProfileResponse(
    val id: String,
    val name: String,
    val email: String,
    val birthDate: LocalDate?,
    val selfIntroduction: String?,
    val majorCategory: MajorCategory?,
    val university: String?,
    val major: String?,
    val highSchool: String?,
    val address: String?,
    val residentNumber: String?,
    val state: UserRoleState,
    val submittedAt: LocalDateTime?,
    val rejectionReason: String?,
    val documents: List<TeacherDocumentResponse>,
) {
    companion object {
        fun from(
            teacher: Teacher,
            userRole: UserRole,
            documents: List<TeacherDocumentResponse>,
        ): TeacherProfileResponse =
            TeacherProfileResponse(
                id = teacher.id,
                name = teacher.user.name,
                email = teacher.user.email,
                birthDate = teacher.profile?.birthDate,
                selfIntroduction = teacher.profile?.selfIntroduction,
                majorCategory = teacher.education?.majorCategory,
                university = teacher.education?.university,
                major = teacher.education?.major,
                highSchool = teacher.education?.highSchool,
                address = teacher.personalInfo?.address,
                residentNumber = teacher.personalInfo?.residentNumber,
                state = userRole.state,
                submittedAt = teacher.verification?.submittedAt,
                rejectionReason = userRole.stateDetail?.rejectionReason,
                documents = documents,
            )
    }
}
