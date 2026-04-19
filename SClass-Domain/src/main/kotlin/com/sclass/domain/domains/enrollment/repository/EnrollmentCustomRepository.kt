package com.sclass.domain.domains.enrollment.repository

import com.sclass.domain.domains.enrollment.domain.EnrollmentStatus
import com.sclass.domain.domains.enrollment.dto.EnrollmentWithCourseDto
import com.sclass.domain.domains.enrollment.dto.EnrollmentWithDetailDto
import com.sclass.domain.domains.enrollment.dto.EnrollmentWithStudentDto
import com.sclass.domain.domains.enrollment.dto.ProductEnrollmentCountDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface EnrollmentCustomRepository {
    fun findAllByCourseIdWithStudent(courseId: Long): List<EnrollmentWithStudentDto>

    fun findAllByStudentUserIdWithCourse(studentUserId: String): List<EnrollmentWithCourseDto>

    fun searchEnrollments(
        studentUserId: String?,
        teacherUserId: String?,
        courseId: Long?,
        status: EnrollmentStatus?,
        pageable: Pageable,
    ): Page<EnrollmentWithDetailDto>

    fun countLiveMembershipEnrollmentsByProductIds(productIds: Collection<String>): List<ProductEnrollmentCountDto>
}
