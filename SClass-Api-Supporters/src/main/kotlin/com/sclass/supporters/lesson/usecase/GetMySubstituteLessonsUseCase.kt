package com.sclass.supporters.lesson.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.domain.domains.lesson.domain.LessonStatus
import com.sclass.supporters.lesson.dto.LessonResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetMySubstituteLessonsUseCase(
    private val lessonAdaptor: LessonAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(
        userId: String,
        status: LessonStatus?,
    ): List<LessonResponse> {
        val lessons = lessonAdaptor.findAllBySubstituteTeacher(userId)
        val filtered = status?.let { s -> lessons.filter { it.status == s } } ?: lessons
        return filtered
            .sortedWith(compareBy(nullsLast()) { it.scheduledAt })
            .map(LessonResponse::from)
    }
}
