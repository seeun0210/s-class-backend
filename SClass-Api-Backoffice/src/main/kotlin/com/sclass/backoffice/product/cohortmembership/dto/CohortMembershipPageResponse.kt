package com.sclass.backoffice.product.cohortmembership.dto

data class CohortMembershipPageResponse(
    val content: List<CohortMembershipResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
)
