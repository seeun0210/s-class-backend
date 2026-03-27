package com.sclass.backoffice.student.dto

import com.sclass.domain.domains.file.domain.File
import com.sclass.domain.domains.organization.domain.AttributionSource
import com.sclass.domain.domains.organization.domain.OrganizationAttribution
import com.sclass.domain.domains.organization.domain.OrganizationUser
import com.sclass.domain.domains.student.domain.Student
import com.sclass.domain.domains.student.domain.StudentDocument
import com.sclass.domain.domains.student.domain.StudentDocumentType
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
    val documents: List<StudentDocumentDetailResponse>,
    val organizations: List<StudentOrganizationResponse>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun from(
            student: Student,
            roles: List<UserRole>,
            documents: List<StudentDocument>,
            organizations: List<OrganizationUser>,
            attributions: List<OrganizationAttribution>,
        ): StudentDetailResponse {
            val attributionByOrgId = attributions.associateBy { it.organizationId }
            return StudentDetailResponse(
                id = student.id,
                name = student.user.name,
                email = student.user.email,
                phoneNumber = student.user.phoneNumber,
                profileImageUrl = student.user.profileImageUrl,
                grade = student.grade,
                school = student.school,
                parentPhoneNumber = student.parentPhoneNumber,
                roles = roles.map { StudentRoleResponse.from(it) },
                documents = documents.map { StudentDocumentDetailResponse.from(it) },
                organizations =
                    organizations.map {
                        StudentOrganizationResponse.from(it, attributionByOrgId[it.organization.id])
                    },
                createdAt = student.createdAt,
                updatedAt = student.updatedAt,
            )
        }
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

data class StudentDocumentDetailResponse(
    val id: String,
    val documentType: StudentDocumentType,
    val file: StudentDocumentFileResponse,
) {
    companion object {
        fun from(document: StudentDocument) =
            StudentDocumentDetailResponse(
                id = document.id,
                documentType = document.documentType,
                file = StudentDocumentFileResponse.from(document.file),
            )
    }
}

data class StudentDocumentFileResponse(
    val id: String,
    val originalFilename: String,
    val mimeType: String,
    val fileSize: Long,
) {
    companion object {
        fun from(file: File) =
            StudentDocumentFileResponse(
                id = file.id,
                originalFilename = file.originalFilename,
                mimeType = file.mimeType,
                fileSize = file.fileSize,
            )
    }
}

data class StudentOrganizationResponse(
    val id: Long,
    val name: String,
    val domain: String,
    val logoUrl: String?,
    val attribution: StudentAttributionResponse?,
) {
    companion object {
        fun from(
            organizationUser: OrganizationUser,
            attribution: OrganizationAttribution?,
        ) = StudentOrganizationResponse(
            id = organizationUser.organization.id,
            name = organizationUser.organization.name,
            domain = organizationUser.organization.domain,
            logoUrl = organizationUser.organization.logoUrl,
            attribution = attribution?.let { StudentAttributionResponse.from(it) },
        )
    }
}

data class StudentAttributionResponse(
    val source: AttributionSource,
    val originService: String?,
) {
    companion object {
        fun from(attribution: OrganizationAttribution) =
            StudentAttributionResponse(
                source = attribution.source,
                originService = attribution.originService,
            )
    }
}
