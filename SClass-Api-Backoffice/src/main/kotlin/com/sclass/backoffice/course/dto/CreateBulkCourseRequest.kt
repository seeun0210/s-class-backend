package com.sclass.backoffice.course.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

data class CreateBulkCourseRequest(
    @field:NotBlank val productId: String,
    @field:NotEmpty val teacherUserIds: List<String>,
    val organizationId: String? = null,
    @field:NotBlank @field:Size(max = 200) val name: String,
    val description: String? = null,
)
