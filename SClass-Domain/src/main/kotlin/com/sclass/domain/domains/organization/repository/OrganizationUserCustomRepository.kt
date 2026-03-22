package com.sclass.domain.domains.organization.repository

import com.sclass.domain.domains.organization.dto.OrganizationUserInfo
import com.sclass.domain.domains.user.domain.Role
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface OrganizationUserCustomRepository {
    fun findUsersByOrganizationIdAndRole(
        organizationId: Long,
        role: Role,
        pageable: Pageable,
    ): Page<OrganizationUserInfo>

    fun countByOrganizationIdGroupByRole(organizationId: Long): Map<Role, Long>
}
