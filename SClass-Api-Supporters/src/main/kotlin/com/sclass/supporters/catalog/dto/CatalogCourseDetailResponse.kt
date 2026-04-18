package com.sclass.supporters.catalog.dto

import com.sclass.domain.domains.course.dto.CourseWithTeacherDto
import com.sclass.domain.domains.teacher.domain.MajorCategory
import java.time.LocalDateTime

data class CatalogCourseDetailResponse(
    val id: Long,
    val productId: String,
    val name: String,
    val description: String?,
    val curriculum: String?,
    val thumbnailUrl: String?,
    val priceWon: Int,
    val totalLessons: Int,
    val maxEnrollments: Int?,
    val remainingSeats: Long?,
    val enrollmentStartAt: LocalDateTime?,
    val enrollmentDeadLine: LocalDateTime?,
    val startAt: LocalDateTime?,
    val endAt: LocalDateTime?,
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
            dto: CourseWithTeacherDto,
            liveEnrollmentCount: Long,
            thumbnailUrl: String?,
        ): CatalogCourseDetailResponse {
            val remaining = dto.course.maxEnrollments?.let { (it - liveEnrollmentCount).coerceAtLeast(0L) }
            return CatalogCourseDetailResponse(
                id = dto.course.id,
                productId = dto.course.productId,
                name = dto.courseProduct?.name ?: "",
                description = dto.courseProduct?.description,
                curriculum = dto.courseProduct?.curriculum,
                thumbnailUrl = thumbnailUrl,
                priceWon = dto.courseProduct?.priceWon ?: 0,
                totalLessons = dto.courseProduct?.totalLessons ?: 0,
                maxEnrollments = dto.course.maxEnrollments,
                remainingSeats = remaining,
                enrollmentStartAt = dto.course.enrollmentStartAt,
                enrollmentDeadLine = dto.course.enrollmentDeadLine,
                startAt = dto.course.startAt,
                endAt = dto.course.endAt,
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
