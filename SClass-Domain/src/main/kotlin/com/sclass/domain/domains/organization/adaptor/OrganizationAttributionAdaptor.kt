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

    fun findByStudentId(studentId: String): OrganizationAttribution =
        organizationAttributionRepository.findByStudentId(studentId) ?: throw OrganizationAttributionNotFoundException()

    fun findByStudentIdOrNull(studentId: String): OrganizationAttribution? = organizationAttributionRepository.findByStudentId(studentId)

    fun findAllByOrganizationId(organizationId: Long): List<OrganizationAttribution> =
        organizationAttributionRepository.findAllByOrganizationId(organizationId)

    fun existsByStudentId(studentId: String): Boolean = organizationAttributionRepository.existsByStudentId(studentId)

    fun save(attribution: OrganizationAttribution): OrganizationAttribution = organizationAttributionRepository.save(attribution)

    fun countByOrganizationId(organizationId: Long): Long = organizationAttributionRepository.countByOrganizationId(organizationId)
}
