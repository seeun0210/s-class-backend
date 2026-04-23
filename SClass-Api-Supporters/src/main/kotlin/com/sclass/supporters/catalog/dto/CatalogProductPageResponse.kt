package com.sclass.supporters.catalog.dto

import org.springframework.data.domain.Page

data class CatalogProductPageResponse(
    val content: List<CatalogProductResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
) {
    companion object {
        fun from(page: Page<CatalogProductResponse>) =
            CatalogProductPageResponse(
                content = page.content,
                totalElements = page.totalElements,
                totalPages = page.totalPages,
                currentPage = page.number,
            )
    }
}
