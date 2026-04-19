package com.sclass.supporters.catalog.dto

data class CatalogCoursePageResponse(
    val content: List<CatalogCourseResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
)
