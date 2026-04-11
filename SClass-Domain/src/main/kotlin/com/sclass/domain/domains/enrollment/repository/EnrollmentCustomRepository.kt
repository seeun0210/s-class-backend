package com.sclass.domain.domains.enrollment.repository

import com.sclass.domain.domains.enrollment.dto.EnrollmentWithStudentDto

interface EnrollmentCustomRepository {
    fun findAllByCourseIdWithStudent(courseId: Long): List<EnrollmentWithStudentDto>
}
