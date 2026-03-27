package com.sclass.domain.domains.organization.service

import com.sclass.common.annotation.DomainService
import com.sclass.domain.domains.organization.adaptor.OrganizationAdaptor
import com.sclass.domain.domains.organization.domain.Organization
import com.sclass.domain.domains.organization.domain.OrganizationSettings
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom

@DomainService
class OrganizationDomainService(
    private val organizationAdaptor: OrganizationAdaptor,
) {
    @Transactional
    fun create(
        name: String,
        domain: String,
        logoUrl: String? = null,
    ): Organization {
        val inviteCode = generateInviteCode()
        return organizationAdaptor.save(
            Organization(
                name = name,
                domain = domain,
                logoUrl = logoUrl,
                inviteCode = inviteCode,
            ),
        )
    }

    private fun generateInviteCode(): String {
        val random = SecureRandom()
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        while (true) {
            val code = (1..6).map { chars[random.nextInt(chars.length)] }.joinToString("")
            if (!organizationAdaptor.existsByInviteCode(code)) {
                return code
            }
        }
    }

    @Transactional
    fun updateSettings(
        id: Long,
        settings: OrganizationSettings,
    ): Organization {
        val organization = organizationAdaptor.findById(id)
        organization.changeSettings(settings)
        return organizationAdaptor.save(organization)
    }

    @Transactional
    fun updateLogoUrl(
        id: Long,
        logoUrl: String?,
    ): Organization {
        val organization = organizationAdaptor.findById(id)
        organization.changeLogoUrl(logoUrl)
        return organizationAdaptor.save(organization)
    }
}
