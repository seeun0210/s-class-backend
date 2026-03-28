package com.sclass.backoffice.organization.dto

import com.sclass.domain.domains.organization.domain.OrganizationUser
import java.time.LocalDateTime

data class OrganizationUserCreateResponse(
    val organizationUserId: String,
    val userId: String,
    val email: String,
    val name: String,
    val organizationId: Long,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(organizationUser: OrganizationUser): OrganizationUserCreateResponse =
            OrganizationUserCreateResponse(
                organizationUserId = organizationUser.id,
                userId = organizationUser.user.id,
                email = organizationUser.user.email,
                name = organizationUser.user.name,
                organizationId = organizationUser.organization.id,
                createdAt = organizationUser.createdAt,
            )
    }
}
