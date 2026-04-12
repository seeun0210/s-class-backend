package com.sclass.supporters.lesson.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.inquiryplan.adaptor.InquiryPlanAdaptor
import com.sclass.domain.domains.inquiryplan.domain.InquiryPlanSourceType
import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.supporters.lesson.dto.LessonDetailResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetLessonDetailUseCase(
    private val lessonAdaptor: LessonAdaptor,
    private val inquiryPlanAdaptor: InquiryPlanAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(lessonId: Long): LessonDetailResponse {
        val lesson = lessonAdaptor.findById(lessonId)
        val latestPlan =
            inquiryPlanAdaptor.findLatestBySourceOrNull(InquiryPlanSourceType.LESSON, lessonId)
        return LessonDetailResponse.from(lesson, latestPlan?.id)
    }
}
