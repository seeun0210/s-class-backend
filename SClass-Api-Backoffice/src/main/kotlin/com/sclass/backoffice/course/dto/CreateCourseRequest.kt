package com.sclass.backoffice.course.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

data class CreateCourseRequest(
    @field:NotBlank val productId: String,
    @field:NotBlank val teacherUserId: String,
    val organizationId: String? = null,
    @field:Min(1) val maxEnrollments: Int? = null,
    val enrollmentStartAt: LocalDateTime? = null,
    val enrollmentDeadLine: LocalDateTime? = null,
    val startAt: LocalDateTime? = null,
    val endAt: LocalDateTime? = null,
)
