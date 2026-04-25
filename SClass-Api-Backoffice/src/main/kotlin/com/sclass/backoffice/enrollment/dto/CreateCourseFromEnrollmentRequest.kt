package com.sclass.backoffice.enrollment.dto

import jakarta.validation.constraints.NotBlank

data class CreateCourseFromEnrollmentRequest(
    @field:NotBlank
    val teacherUserId: String,
)
