package com.sclass.domain.domains.enrollment.repository

import com.sclass.domain.domains.enrollment.domain.EnrollmentStatus
import com.sclass.domain.domains.enrollment.dto.EnrollmentWithDetailDto
import com.sclass.domain.domains.enrollment.dto.EnrollmentWithStudentDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface EnrollmentCustomRepository {
    fun findAllByCourseIdWithStudent(courseId: Long): List<EnrollmentWithStudentDto>

    fun searchEnrollments(
        studentUserId: String?,
        courseId: Long?,
        status: EnrollmentStatus?,
        pageable: Pageable,
    ): Page<EnrollmentWithDetailDto>
}
