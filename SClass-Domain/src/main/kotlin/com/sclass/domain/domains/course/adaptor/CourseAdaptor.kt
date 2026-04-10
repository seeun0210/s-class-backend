package com.sclass.domain.domains.course.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.course.domain.Course
import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.domain.domains.course.exception.CourseNotFoundException
import com.sclass.domain.domains.course.repository.CourseRepository
import org.springframework.data.repository.findByIdOrNull

@Adaptor
class CourseAdaptor(
    private val courseRepository: CourseRepository,
) {
    fun save(course: Course): Course = courseRepository.save(course)

    fun findById(id: Long): Course = courseRepository.findByIdOrNull(id) ?: throw CourseNotFoundException()

    fun findByIdOrNull(id: Long): Course? = courseRepository.findByIdOrNull(id)

    fun findAllByTeacherUserId(teacherUserId: String): List<Course> = courseRepository.findAllByTeacherUserId(teacherUserId)

    fun findActiveByTeacherUserId(teacherUserId: String): List<Course> =
        courseRepository.findAllByTeacherUserIdAndStatus(teacherUserId, CourseStatus.ACTIVE)

    fun findAllByProductId(productId: String): List<Course> = courseRepository.findAllByProductId(productId)

    fun delete(course: Course) = courseRepository.delete(course)
}
