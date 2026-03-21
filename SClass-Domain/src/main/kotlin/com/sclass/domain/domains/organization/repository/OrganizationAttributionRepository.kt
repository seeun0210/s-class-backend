package com.sclass.domain.domains.organization.repository

import com.sclass.domain.domains.organization.domain.OrganizationAttribution
import org.springframework.data.jpa.repository.JpaRepository

interface OrganizationAttributionRepository : JpaRepository<OrganizationAttribution, String> {
    fun findByStudentId(studentId: String): OrganizationAttribution?

    fun findAllByOrganizationId(organizationId: Long): List<OrganizationAttribution>

    fun existsByStudentId(studentId: String): Boolean
}
