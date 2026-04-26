package com.sclass.supporters.lesson.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.inquiryplan.domain.InquiryPlanSourceType
import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.domain.domains.lesson.domain.Lesson
import com.sclass.domain.domains.lesson.domain.LessonStatus
import com.sclass.domain.domains.lesson.domain.LessonType
import com.sclass.domain.domains.lesson.exception.LessonInvalidStatusTransitionException
import com.sclass.domain.domains.lesson.exception.LessonUnauthorizedAccessException
import com.sclass.supporters.inquiry.dto.CreateInquiryPlanRequest
import com.sclass.supporters.inquiry.dto.InquiryPlanResponse
import com.sclass.supporters.inquiry.usecase.CreateInquiryPlanUseCase
import com.sclass.supporters.lesson.dto.CreateLessonInquiryPlanRequest
import org.springframework.transaction.support.TransactionTemplate
import java.time.Clock

@UseCase
class CreateLessonInquiryPlanUseCase(
    private val lessonAdaptor: LessonAdaptor,
    private val createInquiryPlanUseCase: CreateInquiryPlanUseCase,
    private val txTemplate: TransactionTemplate,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    fun execute(
        userId: String,
        lessonId: Long,
        request: CreateLessonInquiryPlanRequest,
    ): InquiryPlanResponse {
        txTemplate.execute {
            val lesson = lessonAdaptor.findById(lessonId)
            if (!lesson.isTeacher(userId)) {
                throw LessonUnauthorizedAccessException()
            }
            startCommissionLessonIfNeeded(lesson, userId)
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

    private fun startCommissionLessonIfNeeded(
        lesson: Lesson,
        userId: String,
    ) {
        if (lesson.lessonType != LessonType.COMMISSION) return
        when (lesson.status) {
            LessonStatus.SCHEDULED -> {
                lesson.start(actualTeacherUserId = userId, clock = clock)
                lessonAdaptor.save(lesson)
            }
            LessonStatus.IN_PROGRESS -> Unit
            LessonStatus.COMPLETED, LessonStatus.CANCELLED -> throw LessonInvalidStatusTransitionException()
        }
    }
}
