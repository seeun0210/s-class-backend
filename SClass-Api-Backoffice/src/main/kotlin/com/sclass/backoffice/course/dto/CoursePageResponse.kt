package com.sclass.backoffice.course.dto

import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.domain.domains.course.dto.CourseWithTeacherAndEnrollmentCountDto
import java.time.LocalDateTime

data class CoursePageResponse(
    val content: List<CourseListResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
)

data class CourseListResponse(
    val id: Long,
    val productId: String,
    val teacherUserId: String,
    val teacherName: String,
    val organizationId: String?,
    val name: String,
    val description: String?,
    val status: CourseStatus,
    val enrollmentCount: Long,
    val totalLessons: Int,
    val priceWon: Int,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(dto: CourseWithTeacherAndEnrollmentCountDto) =
            CourseListResponse(
                id = dto.course.id,
                productId = dto.course.productId,
                teacherUserId = dto.course.teacherUserId,
                teacherName = dto.teacherName,
                organizationId = dto.course.organizationId,
                name = dto.courseProduct?.name ?: "",
                description = dto.courseProduct?.description,
                status = dto.course.status,
                enrollmentCount = dto.enrollmentCount,
                totalLessons = dto.courseProduct?.totalLessons ?: 0,
                priceWon = dto.courseProduct?.priceWon ?: 0,
                createdAt = dto.course.createdAt,
            )
    }
}
