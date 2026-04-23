package com.sclass.backoffice.product.course.dto

import com.sclass.domain.domains.product.domain.CourseProduct

data class CourseProductResponse(
    val productId: String,
    val name: String,
    val description: String?,
    val curriculum: String?,
    val thumbnailUrl: String?,
    val priceWon: Int,
    val totalLessons: Int,
    val requiresMatching: Boolean,
    val visible: Boolean,
) {
    companion object {
        fun from(
            product: CourseProduct,
            thumbnailUrl: String?,
        ): CourseProductResponse =
            CourseProductResponse(
                productId = product.id,
                name = product.name,
                description = product.description,
                curriculum = product.curriculum,
                thumbnailUrl = thumbnailUrl,
                priceWon = product.priceWon,
                totalLessons = product.totalLessons,
                requiresMatching = product.requiresMatching,
                visible = product.visible,
            )
    }
}
