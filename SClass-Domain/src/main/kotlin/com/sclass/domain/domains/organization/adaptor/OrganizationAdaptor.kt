package com.sclass.domain.domains.organization.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.organization.domain.Organization
import com.sclass.domain.domains.organization.exception.OrganizationNotFoundException
import com.sclass.domain.domains.organization.repository.OrganizationRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

@Adaptor
class OrganizationAdaptor(
    private val organizationRepository: OrganizationRepository,
) {
    fun findAll(pageable: Pageable): Page<Organization> = organizationRepository.findAll(pageable)

    fun findById(id: Long): Organization = findByIdOrNull(id) ?: throw OrganizationNotFoundException()

    fun findByIdOrNull(id: Long): Organization? = organizationRepository.findById(id).orElse(null)

    fun findByDomain(domain: String): Organization = findByDomainOrNull(domain) ?: throw OrganizationNotFoundException()

    fun findByDomainOrNull(domain: String): Organization? = organizationRepository.findByDomain(domain)

    fun findByInviteCode(inviteCode: String): Organization = findByInviteCodeOrNull(inviteCode) ?: throw OrganizationNotFoundException()

    fun findByInviteCodeOrNull(inviteCode: String): Organization? = organizationRepository.findByInviteCode(inviteCode)

    fun existsByInviteCode(inviteCode: String): Boolean = organizationRepository.existsByInviteCode(inviteCode)

    fun save(organization: Organization): Organization = organizationRepository.save(organization)
}
