package com.sclass.domain.domains.organization.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.organization.domain.OrganizationUser
import com.sclass.domain.domains.organization.dto.OrganizationUserInfo
import com.sclass.domain.domains.organization.exception.OrganizationUserNotFoundException
import com.sclass.domain.domains.organization.repository.OrganizationUserRepository
import com.sclass.domain.domains.user.domain.Role
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

@Adaptor
class OrganizationUserAdaptor(
    private val organizationUserRepository: OrganizationUserRepository,
) {
    fun findById(id: String): OrganizationUser = organizationUserRepository.findById(id).orElseThrow { OrganizationUserNotFoundException() }

    fun findByUserIdAndOrganizationId(
        userId: String,
        organizationId: Long,
    ): OrganizationUser = findByUserIdAndOrganizationIdOrNull(userId, organizationId) ?: throw OrganizationUserNotFoundException()

    fun findByUserIdAndOrganizationIdOrNull(
        userId: String,
        organizationId: Long,
    ): OrganizationUser? = organizationUserRepository.findByUserIdAndOrganizationId(userId, organizationId)

    fun findAllByOrganizationId(organizationId: Long): List<OrganizationUser> =
        organizationUserRepository.findAllByOrganizationId(organizationId)

    fun findAllByUserId(userId: String): List<OrganizationUser> = organizationUserRepository.findAllByUserId(userId)

    fun existsByUserIdAndOrganizationId(
        userId: String,
        organizationId: Long,
    ): Boolean = organizationUserRepository.existsByUserIdAndOrganizationId(userId, organizationId)

    fun save(organizationUser: OrganizationUser): OrganizationUser = organizationUserRepository.save(organizationUser)

    fun findUsersByOrganizationIdAndRole(
        organizationId: Long,
        role: Role,
        pageable: Pageable,
    ): Page<OrganizationUserInfo> = organizationUserRepository.findUsersByOrganizationIdAndRole(organizationId, role, pageable)

    fun countByOrganizationIdGroupByRole(organizationId: Long): Map<Role, Long> =
        organizationUserRepository.countByOrganizationIdGroupByRole(organizationId)
}
