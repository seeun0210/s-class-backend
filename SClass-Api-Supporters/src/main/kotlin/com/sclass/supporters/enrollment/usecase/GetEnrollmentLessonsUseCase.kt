package com.sclass.supporters.enrollment.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.supporters.lesson.dto.LessonResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetEnrollmentLessonsUseCase(
    private val lessonAdaptor: LessonAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(enrollmentId: Long): List<LessonResponse> = lessonAdaptor.findAllByEnrollment(enrollmentId).map { LessonResponse.from(it) }
}
