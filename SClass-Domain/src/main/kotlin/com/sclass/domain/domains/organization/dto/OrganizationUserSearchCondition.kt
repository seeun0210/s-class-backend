package com.sclass.domain.domains.organization.dto

import com.sclass.domain.domains.user.domain.Role

data class OrganizationUserSearchCondition(
    val name: String? = null,
    val email: String? = null,
    val role: Role? = null,
)
