package com.sclass.backoffice.course.dto

import com.sclass.domain.domains.course.domain.Course
import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.domain.domains.product.domain.CourseProduct

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
        fun from(
            course: Course,
            product: CourseProduct,
        ) = CourseResponse(
            id = course.id,
            productId = course.productId,
            teacherUserId = course.teacherUserId,
            organizationId = course.organizationId,
            name = product.name,
            description = product.description,
            status = course.status,
        )
    }
}
