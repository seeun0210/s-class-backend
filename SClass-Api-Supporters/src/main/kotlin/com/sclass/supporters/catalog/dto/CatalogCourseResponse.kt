package com.sclass.supporters.catalog.dto

import com.sclass.domain.domains.course.dto.CatalogCourseDto
import com.sclass.domain.domains.teacher.domain.MajorCategory
import java.time.LocalDateTime

data class CatalogCourseResponse(
    val id: Long,
    val productId: String,
    val name: String,
    val description: String?,
    val thumbnailUrl: String?,
    val priceWon: Int,
    val totalLessons: Int,
    val maxEnrollments: Int?,
    val remainingSeats: Long?,
    val enrollmentDeadLine: LocalDateTime?,
    val startAt: LocalDateTime?,
    val teacher: TeacherSummary,
) {
    data class TeacherSummary(
        val userId: String,
        val name: String,
        val selfIntroduction: String?,
        val majorCategory: MajorCategory?,
        val university: String?,
        val major: String?,
    )

    companion object {
        fun from(
            dto: CatalogCourseDto,
            thumbnailUrl: String?,
        ): CatalogCourseResponse {
            val remaining = dto.course.maxEnrollments?.let { (it - dto.liveEnrollmentCount).coerceAtLeast(0L) }
            return CatalogCourseResponse(
                id = dto.course.id,
                productId = dto.course.productId,
                name = dto.courseProduct?.name ?: "",
                description = dto.courseProduct?.description,
                thumbnailUrl = thumbnailUrl,
                priceWon = dto.courseProduct?.priceWon ?: 0,
                totalLessons = dto.courseProduct?.totalLessons ?: 0,
                maxEnrollments = dto.course.maxEnrollments,
                remainingSeats = remaining,
                enrollmentDeadLine = dto.course.enrollmentDeadLine,
                startAt = dto.course.startAt,
                teacher =
                    TeacherSummary(
                        userId = dto.course.teacherUserId,
                        name = dto.teacherUser?.name ?: "",
                        selfIntroduction =
                            dto.teacher?.profile?.selfIntroduction,
                        majorCategory = dto.teacher?.education?.majorCategory,
                        university = dto.teacher?.education?.university,
                        major = dto.teacher?.education?.major,
                    ),
            )
        }
    }
}
