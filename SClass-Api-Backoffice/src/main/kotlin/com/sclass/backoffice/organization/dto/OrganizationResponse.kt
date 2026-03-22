package com.sclass.backoffice.organization.dto

import com.sclass.domain.domains.organization.domain.Organization
import java.time.LocalDateTime

data class OrganizationResponse(
    val id: Long,
    val name: String,
    val domain: String,
    val logoUrl: String?,
    val inviteCode: String?,
    val useSupporters: Boolean,
    val useLms: Boolean,
    val status: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun from(org: Organization): OrganizationResponse =
            OrganizationResponse(
                id = org.id,
                name = org.name,
                domain = org.domain,
                logoUrl = org.logoUrl,
                inviteCode = org.inviteCode,
                useSupporters = org.settings.useSupporters,
                useLms = org.settings.useLms,
                status = org.status.name,
                createdAt = org.createdAt,
                updatedAt = org.updatedAt,
            )
    }
}
