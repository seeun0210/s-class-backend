package com.sclass.supporters.catalog.dto

data class CatalogMembershipPageResponse(
    val content: List<CatalogMembershipResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
)
