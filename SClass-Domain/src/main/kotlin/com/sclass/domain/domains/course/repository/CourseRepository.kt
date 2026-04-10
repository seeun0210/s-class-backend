package com.sclass.domain.domains.course.repository

import com.sclass.domain.domains.course.domain.Course
import com.sclass.domain.domains.course.domain.CourseStatus
import org.springframework.data.jpa.repository.JpaRepository

interface CourseRepository : JpaRepository<Course, Long> {
    fun findAllByTeacherUserId(teacherUserId: String): List<Course>

    fun findAllByTeacherUserIdAndStatus(
        teacherUserId: String,
        status: CourseStatus,
    ): List<Course>

    fun findAllByProductId(productId: String): List<Course>
}
