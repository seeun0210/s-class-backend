package com.sclass.backoffice.course.dto

import com.sclass.domain.domains.course.domain.Course
import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.domain.domains.product.domain.CourseProduct
import java.time.LocalDateTime

data class CourseResponse(
    val id: Long,
    val productId: String,
    val teacherUserId: String,
    val organizationId: String?,
    val name: String,
    val description: String?,
    val curriculum: String?,
    val thumbnailUrl: String?,
    val priceWon: Int,
    val totalLessons: Int,
    val status: CourseStatus,
    val maxEnrollments: Int?,
    val enrollmentStartAt: LocalDateTime?,
    val enrollmentDeadLine: LocalDateTime?,
    val startAt: LocalDateTime?,
    val endAt: LocalDateTime?,
) {
    companion object {
        fun from(
            course: Course,
            product: CourseProduct,
            thumbnailUrl: String?,
        ) = CourseResponse(
            id = course.id,
            productId = course.productId,
            teacherUserId = course.teacherUserId,
            organizationId = course.organizationId,
            name = product.name,
            description = product.description,
            curriculum = product.curriculum,
            thumbnailUrl = thumbnailUrl,
            priceWon = product.priceWon,
            totalLessons = product.totalLessons,
            status = course.status,
            maxEnrollments = course.maxEnrollments,
            enrollmentStartAt = course.enrollmentStartAt,
            enrollmentDeadLine = course.enrollmentDeadLine,
            startAt = course.startAt,
            endAt = course.endAt,
        )
    }
}
