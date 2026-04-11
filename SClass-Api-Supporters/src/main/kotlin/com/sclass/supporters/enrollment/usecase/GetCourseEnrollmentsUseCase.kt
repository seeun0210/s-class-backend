package com.sclass.supporters.enrollment.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.course.adaptor.CourseAdaptor
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.supporters.enrollment.dto.EnrollmentWithStudentResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetCourseEnrollmentsUseCase(
    private val courseAdaptor: CourseAdaptor,
    private val enrollmentAdaptor: EnrollmentAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(
        teacherUserId: String,
        courseId: Long,
    ): List<EnrollmentWithStudentResponse> {
        val course = courseAdaptor.findById(courseId)
        require(course.teacherUserId == teacherUserId) { "접근 권한이 없습니다" }
        return enrollmentAdaptor
            .findAllByCourseWithStudent(courseId)
            .map { EnrollmentWithStudentResponse.from(it) }
    }
}
