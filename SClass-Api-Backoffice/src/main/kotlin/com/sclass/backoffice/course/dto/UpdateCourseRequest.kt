package com.sclass.backoffice.course.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class UpdateCourseRequest(
    @field:Size(max = 200) val name: String? = null,
    val description: String? = null,
    val curriculum: String? = null,
    val thumbnailFileId: String? = null,
    @field:Min(0) val priceWon: Int? = null,
    @field:Min(1) val maxEnrollments: Int? = null,
    val enrollmentStartAt: LocalDateTime? = null,
    val enrollmentDeadLine: LocalDateTime? = null,
    val startAt: LocalDateTime? = null,
    val endAt: LocalDateTime? = null,
) {
    fun hasEnrollmentConstraintChange() = maxEnrollments != null || enrollmentStartAt != null || enrollmentDeadLine != null

    fun hasScheduleChange() = startAt != null || endAt != null
}
