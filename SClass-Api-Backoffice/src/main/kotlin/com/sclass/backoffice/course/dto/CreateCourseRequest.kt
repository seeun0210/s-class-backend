package com.sclass.backoffice.course.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class CreateCourseRequest(
    @field:NotBlank val teacherUserId: String,
    val organizationId: String? = null,
    @field:NotBlank @field:Size(max = 200) val name: String,
    val description: String? = null,
    val curriculum: String? = null,
    val thumbnailFileId: String? = null,
    @field:Min(0) val priceWon: Int,
    @field:Min(1) val totalLessons: Int,
    @field:Min(1) val maxEnrollments: Int? = null,
    val enrollmentStartAt: LocalDateTime? = null,
    val enrollmentDeadLine: LocalDateTime? = null,
    val startAt: LocalDateTime? = null,
    val endAt: LocalDateTime? = null,
)
