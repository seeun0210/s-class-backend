package com.sclass.backoffice.teacher.dto

import com.sclass.domain.domains.file.domain.File
import com.sclass.domain.domains.organization.domain.OrganizationUser
import com.sclass.domain.domains.teacher.domain.Teacher
import com.sclass.domain.domains.teacher.domain.TeacherContract
import com.sclass.domain.domains.teacher.domain.TeacherDocument
import com.sclass.domain.domains.teacher.domain.TeacherDocumentType
import com.sclass.domain.domains.teacher.domain.TeacherEducation
import com.sclass.domain.domains.teacher.domain.TeacherPersonalInfo
import com.sclass.domain.domains.teacher.domain.TeacherProfile
import com.sclass.domain.domains.teacher.domain.TeacherVerification
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.Role
import com.sclass.domain.domains.user.domain.User
import com.sclass.domain.domains.user.domain.UserRole
import com.sclass.domain.domains.user.domain.UserRoleState
import java.time.LocalDateTime

data class TeacherDetailResponse(
    val id: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val user: TeacherUserInfo,
    val profile: TeacherProfile,
    val education: TeacherEducation,
    val personalInfo: TeacherPersonalInfo,
    val contract: TeacherContract,
    val verification: TeacherVerification,
    val roles: List<TeacherRoleResponse>,
    val documents: List<TeacherDocumentDetailResponse>,
    val organizations: List<TeacherOrganizationResponse>,
) {
    companion object {
        fun from(
            teacher: Teacher,
            roles: List<UserRole>,
            documents: List<TeacherDocument>,
            organizations: List<OrganizationUser>,
        ) = TeacherDetailResponse(
            id = teacher.id,
            createdAt = teacher.createdAt,
            updatedAt = teacher.updatedAt,
            user = TeacherUserInfo.from(teacher.user),
            profile = teacher.profile,
            education = teacher.education,
            personalInfo = teacher.personalInfo,
            contract = teacher.contract,
            verification = teacher.verification,
            roles = roles.map { TeacherRoleResponse.from(it) },
            documents = documents.map { TeacherDocumentDetailResponse.from(it) },
            organizations = organizations.map { TeacherOrganizationResponse.from(it) },
        )
    }
}

data class TeacherUserInfo(
    val name: String,
    val email: String,
    val phoneNumber: String?,
    val profileImageUrl: String?,
) {
    companion object {
        fun from(user: User) =
            TeacherUserInfo(
                name = user.name,
                email = user.email,
                phoneNumber = user.phoneNumber,
                profileImageUrl = user.profileImageUrl,
            )
    }
}

data class TeacherRoleResponse(
    val platform: Platform,
    val role: Role,
    val state: UserRoleState,
) {
    companion object {
        fun from(userRole: UserRole) =
            TeacherRoleResponse(
                platform = userRole.platform,
                role = userRole.role,
                state = userRole.state,
            )
    }
}

data class TeacherDocumentDetailResponse(
    val id: String,
    val documentType: TeacherDocumentType,
    val file: TeacherDocumentFileResponse,
) {
    companion object {
        fun from(document: TeacherDocument) =
            TeacherDocumentDetailResponse(
                id = document.id,
                documentType = document.documentType,
                file = TeacherDocumentFileResponse.from(document.file),
            )
    }
}

data class TeacherDocumentFileResponse(
    val id: String,
    val originalFilename: String,
    val mimeType: String,
    val fileSize: Long,
) {
    companion object {
        fun from(file: File) =
            TeacherDocumentFileResponse(
                id = file.id,
                originalFilename = file.originalFilename,
                mimeType = file.mimeType,
                fileSize = file.fileSize,
            )
    }
}

data class TeacherOrganizationResponse(
    val id: Long,
    val name: String,
    val domain: String,
    val logoUrl: String?,
) {
    companion object {
        fun from(organizationUser: OrganizationUser) =
            TeacherOrganizationResponse(
                id = organizationUser.organization.id,
                name = organizationUser.organization.name,
                domain = organizationUser.organization.domain,
                logoUrl = organizationUser.organization.logoUrl,
            )
    }
}
