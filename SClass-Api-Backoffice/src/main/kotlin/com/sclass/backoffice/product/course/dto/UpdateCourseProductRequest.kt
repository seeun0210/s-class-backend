package com.sclass.backoffice.product.course.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size

data class UpdateCourseProductRequest(
    @field:Size(max = 200)
    val name: String? = null,
    val description: String? = null,
    val curriculum: String? = null,
    val thumbnailFileId: String? = null,
    @field:Min(0)
    val priceWon: Int? = null,
    @field:Min(1)
    val totalLessons: Int? = null,
    val requiresMatching: Boolean? = null,
    val visible: Boolean? = null,
)
