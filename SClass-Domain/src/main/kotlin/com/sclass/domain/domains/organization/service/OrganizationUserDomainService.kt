package com.sclass.domain.domains.organization.service

import com.sclass.common.annotation.DomainService
import com.sclass.domain.domains.organization.adaptor.OrganizationAdaptor
import com.sclass.domain.domains.organization.adaptor.OrganizationUserAdaptor
import com.sclass.domain.domains.organization.dto.OrganizationUserInfo
import com.sclass.domain.domains.user.domain.Role
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional

@DomainService
class OrganizationUserDomainService(
    private val organizationAdaptor: OrganizationAdaptor,
    private val organizationUserAdaptor: OrganizationUserAdaptor,
) {
    @Transactional(readOnly = true)
    fun getUsersByRole(
        organizationId: Long,
        role: Role,
        pageable: Pageable,
    ): Page<OrganizationUserInfo> {
        organizationAdaptor.findById(organizationId)
        return organizationUserAdaptor.findUsersByOrganizationIdAndRole(organizationId, role, pageable)
    }

    @Transactional(readOnly = true)
    fun getUserStats(organizationId: Long): Map<Role, Long> {
        organizationAdaptor.findById(organizationId)
        return organizationUserAdaptor.countByOrganizationIdGroupByRole(organizationId)
    }
}
