package com.sclass.backoffice.course.dto

import com.sclass.domain.domains.course.domain.Course
import com.sclass.domain.domains.course.domain.CourseStatus

data class CourseResponse(
    val id: Long,
    val productId: String,
    val teacherUserId: String,
    val organizationId: String?,
    val name: String,
    val description: String?,
    val status: CourseStatus,
) {
    companion object {
        fun from(course: Course) =
            CourseResponse(
                id = course.id,
                productId = course.productId,
                teacherUserId = course.teacherUserId,
                organizationId = course.organizationId,
                name = course.name,
                description = course.description,
                status = course.status,
            )
    }
}
