package com.sclass.domain.domains.organization.service

import com.sclass.common.annotation.DomainService
import com.sclass.domain.domains.organization.adaptor.OrganizationAttributionAdaptor
import com.sclass.domain.domains.organization.domain.AttributionSource
import com.sclass.domain.domains.organization.domain.OrganizationAttribution
import com.sclass.domain.domains.organization.exception.OrganizationAlreadyAttributedException
import org.springframework.transaction.annotation.Transactional

@DomainService
class OrganizationAttributionDomainService(
    private val organizationAttributionAdaptor: OrganizationAttributionAdaptor,
) {
    @Transactional
    fun attribute(
        organizationId: Long,
        studentId: String,
        source: AttributionSource,
        originService: String? = null,
    ): OrganizationAttribution {
        if (organizationAttributionAdaptor.existsByStudentId(studentId)) {
            throw OrganizationAlreadyAttributedException()
        }
        return organizationAttributionAdaptor.save(
            OrganizationAttribution(
                organizationId = organizationId,
                studentId = studentId,
                source = source,
                originService = originService,
            ),
        )
    }

    @Transactional(readOnly = true)
    fun isAttributed(studentId: String): Boolean = organizationAttributionAdaptor.existsByStudentId(studentId)

    @Transactional(readOnly = true)
    fun findByStudentId(studentId: String): OrganizationAttribution = organizationAttributionAdaptor.findByStudentId(studentId)

    @Transactional(readOnly = true)
    fun findAllByOrganizationId(organizationId: Long): List<OrganizationAttribution> =
        organizationAttributionAdaptor.findAllByOrganizationId(organizationId)

    @Transactional(readOnly = true)
    fun countByOrganizationId(organizationId: Long): Long = organizationAttributionAdaptor.countByOrganizationId(organizationId)
}
