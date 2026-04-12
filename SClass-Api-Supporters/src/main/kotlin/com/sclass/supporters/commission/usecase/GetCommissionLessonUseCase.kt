package com.sclass.supporters.commission.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.commission.adaptor.CommissionAdaptor
import com.sclass.domain.domains.commission.exception.CommissionUnauthorizedAccessException
import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.domain.domains.lesson.exception.LessonNotFoundException
import com.sclass.supporters.lesson.dto.LessonResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetCommissionLessonUseCase(
    private val commissionAdaptor: CommissionAdaptor,
    private val lessonAdaptor: LessonAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(
        userId: String,
        commissionId: Long,
    ): LessonResponse {
        val commission = commissionAdaptor.findById(commissionId)
        if (commission.studentUserId != userId && commission.teacherUserId != userId) {
            throw CommissionUnauthorizedAccessException()
        }
        val lesson = lessonAdaptor.findByCommission(commissionId) ?: throw LessonNotFoundException()
        return LessonResponse.from(lesson)
    }
}
