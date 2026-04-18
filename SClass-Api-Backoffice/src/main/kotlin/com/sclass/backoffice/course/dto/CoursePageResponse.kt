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
    val thumbnailFileId: String?,
    val status: CourseStatus,
    val enrollmentCount: Long,
    val maxEnrollments: Int,
    val totalLessons: Int,
    val priceWon: Int,
    val enrollmentStartAt: LocalDateTime?,
    val enrollmentDeadLine: LocalDateTime?,
    val startAt: LocalDateTime?,
    val endAt: LocalDateTime?,
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
                thumbnailFileId = dto.courseProduct?.thumbnailFileId,
                status = dto.course.status,
                enrollmentCount = dto.enrollmentCount,
                maxEnrollments = dto.course.maxEnrollments,
                totalLessons = dto.courseProduct?.totalLessons ?: 0,
                priceWon = dto.courseProduct?.priceWon ?: 0,
                enrollmentStartAt = dto.course.enrollmentStartAt,
                enrollmentDeadLine = dto.course.enrollmentDeadLine,
                startAt = dto.course.startAt,
                endAt = dto.course.endAt,
                createdAt = dto.course.createdAt,
            )
    }
}
