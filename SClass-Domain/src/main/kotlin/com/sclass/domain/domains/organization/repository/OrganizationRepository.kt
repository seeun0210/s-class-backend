package com.sclass.domain.domains.organization.repository

import com.sclass.domain.domains.organization.domain.Organization
import org.springframework.data.jpa.repository.JpaRepository

interface OrganizationRepository : JpaRepository<Organization, Long> {
    fun findByDomain(domain: String): Organization?

    fun findByInviteCode(inviteCode: String): Organization?

    fun existsByInviteCode(inviteCode: String): Boolean
}
