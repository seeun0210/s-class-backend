package com.sclass.backoffice.course.dto

import jakarta.validation.constraints.Min
import java.time.LocalDateTime

data class UpdateCourseRequest(
    val curriculum: String? = null,
    @field:Min(1) val totalLessons: Int? = null,
    @field:Min(1) val maxEnrollments: Int? = null,
    val enrollmentStartAt: LocalDateTime? = null,
    val enrollmentDeadLine: LocalDateTime? = null,
    val startAt: LocalDateTime? = null,
    val endAt: LocalDateTime? = null,
) {
    fun hasEnrollmentConstraintChange() = maxEnrollments != null || enrollmentStartAt != null || enrollmentDeadLine != null

    fun hasScheduleChange() = startAt != null || endAt != null
}
