package com.sclass.backoffice.organization.dto

import com.sclass.domain.domains.organization.dto.OrganizationUserInfo
import org.springframework.data.domain.Page

data class OrganizationUserPageResponse(
    val content: List<OrganizationUserResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
) {
    companion object {
        fun from(page: Page<OrganizationUserInfo>): OrganizationUserPageResponse =
            OrganizationUserPageResponse(
                content = page.content.map(OrganizationUserResponse::from),
                page = page.number,
                size = page.size,
                totalElements = page.totalElements,
                totalPages = page.totalPages,
            )
    }
}
