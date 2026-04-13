package com.sclass.supporters.lesson.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.inquiryplan.domain.InquiryPlanSourceType
import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.domain.domains.lesson.exception.LessonUnauthorizedAccessException
import com.sclass.supporters.inquiry.dto.CreateInquiryPlanRequest
import com.sclass.supporters.inquiry.dto.InquiryPlanResponse
import com.sclass.supporters.inquiry.usecase.CreateInquiryPlanUseCase
import com.sclass.supporters.lesson.dto.CreateLessonInquiryPlanRequest

@UseCase
class CreateLessonInquiryPlanUseCase(
    private val lessonAdaptor: LessonAdaptor,
    private val createInquiryPlanUseCase: CreateInquiryPlanUseCase,
) {
    fun execute(
        userId: String,
        lessonId: Long,
        request: CreateLessonInquiryPlanRequest,
    ): InquiryPlanResponse {
        val lesson = lessonAdaptor.findById(lessonId)
        if (!lesson.isTeacher(userId)) {
            throw LessonUnauthorizedAccessException()
        }
        return createInquiryPlanUseCase.execute(
            userId,
            CreateInquiryPlanRequest(
                paragraph = request.paragraph,
                sourceType = InquiryPlanSourceType.LESSON,

                sourceRefId = lessonId,
            ),
        )
    }
}
