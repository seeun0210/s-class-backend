package com.sclass.backoffice.organization.dto

import com.sclass.domain.domains.organization.dto.OrganizationUserInfo
import java.time.LocalDateTime

data class OrganizationUserResponse(
    val userId: String,
    val name: String,
    val email: String,
    val role: String,
    val profileImageUrl: String?,
    val createdAt: LocalDateTime,
    val source: String?,
    val originService: String?,
) {
    companion object {
        fun from(info: OrganizationUserInfo): OrganizationUserResponse =
            OrganizationUserResponse(
                userId = info.userId,
                name = info.name,
                email = info.email,
                role = info.role?.name ?: "UNKNOWN",
                profileImageUrl = info.profileImageUrl,
                createdAt = info.createdAt,
                source = info.source?.name,
                originService = info.originService,
            )
    }
}
