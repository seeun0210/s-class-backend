package com.sclass.supporters.catalog.dto

import com.sclass.domain.domains.course.dto.CourseWithTeacherDto
import com.sclass.domain.domains.teacher.domain.MajorCategory

data class CatalogCourseResponse(
    val id: Long,
    val productId: String,
    val name: String,
    val description: String?,
    val priceWon: Int,
    val totalLessons: Int,
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
        fun from(dto: CourseWithTeacherDto) =
            CatalogCourseResponse(
                id = dto.course.id,
                productId = dto.course.productId,
                name = dto.courseProduct?.name ?: "",
                description = dto.courseProduct?.description,
                priceWon = dto.courseProduct?.priceWon ?: 0,
                totalLessons = dto.courseProduct?.totalLessons ?: 0,
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
