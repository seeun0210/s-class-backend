package com.sclass.supporters.lesson.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.domain.domains.lesson.exception.LessonUnauthorizedAccessException
import com.sclass.supporters.lesson.dto.CompleteLessonRequest
import com.sclass.supporters.lesson.dto.LessonResponse
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@UseCase
class CompleteLessonUseCase(
    private val lessonAdaptor: LessonAdaptor,
) {
    @Transactional
    fun execute(
        userId: String,
        lessonId: Long,
        request: CompleteLessonRequest,
    ): LessonResponse {
        val lesson = lessonAdaptor.findById(lessonId)
        if (!lesson.isTeacher(userId)) throw LessonUnauthorizedAccessException()
        lesson.complete(actualTeacherUserId = userId, at = request.completedAt ?: LocalDateTime.now())
        return LessonResponse.from(lesson)
    }
}
