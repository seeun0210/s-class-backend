package com.sclass.supporters.enrollment.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.enrollment.exception.EnrollmentUnauthorizedAccessException
import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.supporters.lesson.dto.LessonResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetEnrollmentLessonsUseCase(
    private val enrollmentAdaptor: EnrollmentAdaptor,
    private val lessonAdaptor: LessonAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(
        userId: String,
        enrollmentId: Long,
    ): List<LessonResponse> {
        val enrollment = enrollmentAdaptor.findById(enrollmentId)
        if (enrollment.studentUserId != userId) {
            throw EnrollmentUnauthorizedAccessException()
        }
        return lessonAdaptor.findAllByEnrollment(enrollmentId).map { LessonResponse.from(it) }
    }
}
