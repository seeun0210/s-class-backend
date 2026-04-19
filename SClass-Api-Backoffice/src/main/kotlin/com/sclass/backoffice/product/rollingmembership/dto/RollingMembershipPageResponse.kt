package com.sclass.backoffice.product.rollingmembership.dto

data class RollingMembershipPageResponse(
    val content: List<RollingMembershipResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
)
