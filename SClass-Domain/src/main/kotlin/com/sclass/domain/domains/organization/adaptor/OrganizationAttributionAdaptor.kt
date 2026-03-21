package com.sclass.domain.domains.organization.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.organization.domain.OrganizationAttribution
import com.sclass.domain.domains.organization.exception.OrganizationAttributionNotFoundException
import com.sclass.domain.domains.organization.repository.OrganizationAttributionRepository

@Adaptor
class OrganizationAttributionAdaptor(
    private val organizationAttributionRepository: OrganizationAttributionRepository,
) {
    fun findById(id: String): OrganizationAttribution =
        organizationAttributionRepository.findById(id).orElseThrow { OrganizationAttributionNotFoundException() }

    fun findByUserId(userId: String): OrganizationAttribution =
        organizationAttributionRepository.findByUserId(userId) ?: throw OrganizationAttributionNotFoundException()

    fun findByUserIdOrNull(userId: String): OrganizationAttribution? = organizationAttributionRepository.findByUserId(userId)

    fun findAllByOrganizationId(organizationId: Long): List<OrganizationAttribution> =
        organizationAttributionRepository.findAllByOrganizationId(organizationId)

    fun existsByUserId(userId: String): Boolean = organizationAttributionRepository.existsByUserId(userId)

    fun save(attribution: OrganizationAttribution): OrganizationAttribution = organizationAttributionRepository.save(attribution)
}
