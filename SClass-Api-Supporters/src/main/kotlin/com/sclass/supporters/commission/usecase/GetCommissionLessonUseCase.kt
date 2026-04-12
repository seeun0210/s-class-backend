package com.sclass.supporters.commission.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.domain.domains.lesson.exception.LessonNotFoundException
import com.sclass.supporters.lesson.dto.LessonResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetCommissionLessonUseCase(
    private val lessonAdaptor: LessonAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(commissionId: Long): LessonResponse {
        val lesson = lessonAdaptor.findByCommission(commissionId) ?: throw LessonNotFoundException()
        return LessonResponse.from(lesson)
    }
}
