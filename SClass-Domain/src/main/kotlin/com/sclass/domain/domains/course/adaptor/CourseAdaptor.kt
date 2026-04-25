package com.sclass.domain.domains.course.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.course.domain.Course
import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.domain.domains.course.dto.CatalogCourseDto
import com.sclass.domain.domains.course.dto.CourseWithEnrollmentCountDto
import com.sclass.domain.domains.course.dto.CourseWithTeacherAndEnrollmentCountDto
import com.sclass.domain.domains.course.exception.CourseNotFoundException
import com.sclass.domain.domains.course.repository.CourseRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull

@Adaptor
class CourseAdaptor(
    private val courseRepository: CourseRepository,
) {
    fun save(course: Course): Course = courseRepository.save(course)

    fun saveAll(courses: List<Course>): List<Course> = courseRepository.saveAll(courses)

    fun findById(id: Long): Course = courseRepository.findByIdOrNull(id) ?: throw CourseNotFoundException()

    fun findByIdOrNull(id: Long): Course? = courseRepository.findByIdOrNull(id)

    fun findCourseDetailById(id: Long): CourseWithTeacherAndEnrollmentCountDto =
        courseRepository.findCourseDetailById(id) ?: throw CourseNotFoundException()

    fun findAllByTeacherUserIdWithEnrollmentCount(teacherUserId: String): List<CourseWithEnrollmentCountDto> =
        courseRepository.findAllByTeacherUserIdWithEnrollmentCount(teacherUserId)

    fun searchCourses(
        teacherUserId: String?,
        status: CourseStatus?,
        pageable: Pageable,
    ): Page<CourseWithTeacherAndEnrollmentCountDto> = courseRepository.searchCourses(teacherUserId, status, pageable)

    fun findAllByTeacherUserId(teacherUserId: String): List<Course> = courseRepository.findAllByTeacherUserId(teacherUserId)

    fun findAllByProductId(productId: String): List<Course> = courseRepository.findAllByProductId(productId)

    fun delete(course: Course) = courseRepository.delete(course)

    fun findAllCatalogCoursesByProductIds(productIds: Collection<String>): Map<String, List<CatalogCourseDto>> =
        courseRepository
            .findAllCatalogCoursesByProductIds(productIds)
            .groupBy { it.course.productId }

    fun findAllCatalogCoursesByProductId(productId: String): List<CatalogCourseDto> =
        findAllCatalogCoursesByProductIds(listOf(productId))[productId].orEmpty()
}
