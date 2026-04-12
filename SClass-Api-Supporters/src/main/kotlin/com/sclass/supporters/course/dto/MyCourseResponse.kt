package com.sclass.supporters.course.dto

import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.domain.domains.course.dto.CourseWithEnrollmentCountDto

data class MyCourseResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val productId: String,
    val status: CourseStatus,
    val enrollmentCount: Long,
) {
    companion object {
        fun from(dto: CourseWithEnrollmentCountDto) =
            MyCourseResponse(
                id = dto.course.id,
                name = dto.course.name,
                description = dto.course.description,
                productId = dto.course.productId,
                status = dto.course.status,
                enrollmentCount = dto.enrollmentCount,
            )
    }
}
