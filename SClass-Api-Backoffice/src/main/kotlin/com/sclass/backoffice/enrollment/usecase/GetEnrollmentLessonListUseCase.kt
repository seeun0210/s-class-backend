package com.sclass.backoffice.enrollment.usecase

import com.sclass.backoffice.enrollment.dto.EnrollmentLessonListResponse
import com.sclass.backoffice.enrollment.dto.EnrollmentLessonResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetEnrollmentLessonListUseCase(
    private val lessonAdaptor: LessonAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(enrollmentId: Long): EnrollmentLessonListResponse {
        val lessons = lessonAdaptor.findAllByEnrollment(enrollmentId)
        return EnrollmentLessonListResponse(
            lessons = lessons.map { EnrollmentLessonResponse.from(it) },
        )
    }
}
