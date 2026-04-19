package com.sclass.supporters.course.dto

import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.domain.domains.course.dto.CourseWithEnrollmentCountDto
import java.time.LocalDateTime

data class MyCourseResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val productId: String,
    val status: CourseStatus,
    val enrollmentCount: Long,
    val maxEnrollments: Int?,
    val enrollmentDeadLine: LocalDateTime?,
    val startAt: LocalDateTime?,
    val endAt: LocalDateTime?,
) {
    companion object {
        fun from(dto: CourseWithEnrollmentCountDto) =
            MyCourseResponse(
                id = dto.course.id,
                name = dto.courseProduct?.name ?: "",
                description = dto.courseProduct?.description,
                productId = dto.course.productId,
                status = dto.course.status,
                enrollmentCount = dto.enrollmentCount,
                maxEnrollments = dto.course.maxEnrollments,
                enrollmentDeadLine = dto.course.enrollmentDeadLine,
                startAt = dto.course.startAt,
                endAt = dto.course.endAt,
            )
    }
}
