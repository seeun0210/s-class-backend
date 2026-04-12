package com.sclass.supporters.lesson.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.inquiryplan.adaptor.InquiryPlanAdaptor
import com.sclass.domain.domains.inquiryplan.domain.InquiryPlanSourceType
import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.domain.domains.lesson.exception.LessonUnauthorizedAccessException
import com.sclass.supporters.lesson.dto.LessonDetailResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetLessonDetailUseCase(
    private val lessonAdaptor: LessonAdaptor,
    private val inquiryPlanAdaptor: InquiryPlanAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(
        userId: String,
        lessonId: Long,
    ): LessonDetailResponse {
        val lesson = lessonAdaptor.findById(lessonId)
        if (lesson.studentUserId != userId && lesson.assignedTeacherUserId != userId) {
            throw LessonUnauthorizedAccessException()
        }
        val latestPlan =
            inquiryPlanAdaptor.findLatestBySourceOrNull(InquiryPlanSourceType.LESSON, lessonId)
        return LessonDetailResponse.from(lesson, latestPlan?.id)
    }
}
