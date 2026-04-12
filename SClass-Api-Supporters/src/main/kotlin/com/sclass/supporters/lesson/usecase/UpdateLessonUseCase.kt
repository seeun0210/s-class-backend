package com.sclass.supporters.lesson.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.domain.domains.lesson.exception.LessonUnauthorizedAccessException
import com.sclass.supporters.lesson.dto.LessonResponse
import com.sclass.supporters.lesson.dto.UpdateLessonRequest
import org.springframework.transaction.annotation.Transactional

@UseCase
class UpdateLessonUseCase(
    private val lessonAdaptor: LessonAdaptor,
) {
    @Transactional
    fun execute(
        userId: String,
        lessonId: Long,
        request: UpdateLessonRequest,
    ): LessonResponse {
        val lesson = lessonAdaptor.findById(lessonId)
        if (lesson.assignedTeacherUserId != userId) {
            throw LessonUnauthorizedAccessException()
        }
        lesson.updateSchedule(request.name, request.scheduledAt)
        return LessonResponse.from(lessonAdaptor.save(lesson))
    }
}
