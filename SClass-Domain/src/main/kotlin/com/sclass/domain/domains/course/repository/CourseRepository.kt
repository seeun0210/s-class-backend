package com.sclass.domain.domains.course.repository

import com.sclass.domain.domains.course.domain.Course
import org.springframework.data.jpa.repository.JpaRepository

interface CourseRepository :
    JpaRepository<Course, Long>,
    CourseCustomRepository {
    fun findAllByTeacherUserId(teacherUserId: String): List<Course>

    fun findAllByProductId(productId: String): List<Course>
}
