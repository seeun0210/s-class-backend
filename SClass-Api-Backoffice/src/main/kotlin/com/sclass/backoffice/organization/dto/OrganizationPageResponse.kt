package com.sclass.backoffice.organization.dto

import com.sclass.domain.domains.organization.domain.Organization
import org.springframework.data.domain.Page

data class OrganizationPageResponse(
    val content: List<OrganizationResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
) {
    companion object {
        fun from(page: Page<Organization>): OrganizationPageResponse =
            OrganizationPageResponse(
                content = page.content.map(OrganizationResponse::from),
                page = page.number,
                size = page.size,
                totalElements = page.totalElements,
                totalPages = page.totalPages,
            )
    }
}
