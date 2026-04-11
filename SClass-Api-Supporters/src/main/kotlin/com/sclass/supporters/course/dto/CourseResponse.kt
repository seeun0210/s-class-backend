package com.sclass.supporters.course.dto

import com.sclass.domain.domains.course.dto.CourseWithTeacherDto
import com.sclass.domain.domains.teacher.domain.MajorCategory

data class CourseResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val productId: String,
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
            CourseResponse(
                id = dto.course.id,
                name = dto.course.name,
                description = dto.course.description,
                productId = dto.course.productId,
                teacher =
                    TeacherSummary(
                        userId = dto.course.teacherUserId,
                        name = dto.teacherUser?.name ?: "",
                        selfIntroduction = dto.teacher?.profile?.selfIntroduction,
                        majorCategory = dto.teacher?.education?.majorCategory,
                        university = dto.teacher?.education?.university,
                        major = dto.teacher?.education?.major,
                    ),
            )
    }
}
