package com.sclass.backoffice.lesson.usecase

import com.sclass.backoffice.lesson.dto.LessonResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetLessonDetailUseCase(
    private val lessonAdaptor: LessonAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(lessonId: Long): LessonResponse {
        val lesson = lessonAdaptor.findById(lessonId)
        return LessonResponse.from(lesson)
    }
}
