package com.sclass.supporters.lesson.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.domain.domains.lesson.exception.LessonUnauthorizedAccessException
import com.sclass.supporters.lesson.dto.LessonResponse
import com.sclass.supporters.lesson.dto.RecordLessonRequest
import org.springframework.transaction.annotation.Transactional

@UseCase
class RecordLessonUseCase(
    private val lessonAdaptor: LessonAdaptor,
) {
    @Transactional
    fun execute(
        userId: String,
        lessonId: Long,
        request: RecordLessonRequest,
    ): LessonResponse {
        val lesson = lessonAdaptor.findById(lessonId)
        if (!lesson.isTeacher(userId)) throw LessonUnauthorizedAccessException()
        lesson.record(
            actualTeacherUserId = userId,
            startedAt = request.startedAt,
            completedAt = request.completedAt,
        )
        return LessonResponse.from(lesson)
    }
}
