package com.sclass.backoffice.product.course.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateCourseProductRequest(
    @field:NotBlank
    @field:Size(max = 200)
    val name: String,
    val description: String? = null,
    val curriculum: String? = null,
    val thumbnailFileId: String? = null,
    @field:Min(0)
    val priceWon: Int,
    @field:Min(1)
    val totalLessons: Int,
    val requiresMatching: Boolean = false,
    val visible: Boolean = false,
)
