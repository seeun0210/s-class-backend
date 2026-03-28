package com.sclass.domain.domains.organization.dto

import com.sclass.domain.domains.organization.domain.AttributionSource
import com.sclass.domain.domains.user.domain.Role
import java.time.LocalDateTime

data class OrganizationUserInfo(
    val userId: String,
    val name: String,
    val email: String,
    val profileImageUrl: String?,
    val role: Role?,
    val createdAt: LocalDateTime,
    val source: AttributionSource? = null,
    val originService: String? = null,
)
