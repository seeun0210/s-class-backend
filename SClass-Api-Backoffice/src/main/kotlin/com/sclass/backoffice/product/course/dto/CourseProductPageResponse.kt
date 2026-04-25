package com.sclass.backoffice.product.course.dto

import org.springframework.data.domain.Page

data class CourseProductPageResponse(
    val content: List<CourseProductResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
) {
    companion object {
        fun from(page: Page<CourseProductResponse>) =
            CourseProductPageResponse(
                content = page.content,
                totalElements = page.totalElements,
                totalPages = page.totalPages,
                currentPage = page.number,
            )
    }
}
