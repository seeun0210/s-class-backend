package com.sclass.domain.domains.organization.repository

import com.sclass.domain.domains.organization.domain.OrganizationUser
import org.springframework.data.jpa.repository.JpaRepository

interface OrganizationUserRepository : JpaRepository<OrganizationUser, String> {
    fun findByUserIdAndOrganizationId(
        userId: String,
        organizationId: Long,
    ): OrganizationUser?

    fun findAllByOrganizationId(organizationId: Long): List<OrganizationUser>

    fun findAllByUserId(userId: String): List<OrganizationUser>

    fun existsByUserIdAndOrganizationId(
        userId: String,
        organizationId: Long,
    ): Boolean
}
