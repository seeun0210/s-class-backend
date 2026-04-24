package com.sclass.supporters.commission.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.common.exception.BusinessException
import com.sclass.common.exception.GlobalErrorCode
import com.sclass.domain.domains.commission.adaptor.CommissionAdaptor
import com.sclass.domain.domains.commission.adaptor.CommissionTopicAdaptor
import com.sclass.domain.domains.commission.domain.CommissionStatus
import com.sclass.domain.domains.commission.exception.CommissionErrorCode
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.enrollment.exception.NoActiveMembershipException
import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.domain.domains.lesson.domain.Lesson
import com.sclass.domain.domains.lesson.domain.LessonType
import com.sclass.supporters.commission.dto.CommissionTopicResponse
import com.sclass.supporters.commission.dto.SelectTopicRequest
import com.sclass.supporters.commission.scheduler.CommissionReminderScheduler
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDateTime

@UseCase
class SelectTopicUseCase(
    private val commissionAdaptor: CommissionAdaptor,
    private val commissionTopicAdaptor: CommissionTopicAdaptor,
    private val lessonAdaptor: LessonAdaptor,
    private val enrollmentAdaptor: EnrollmentAdaptor,
    private val commissionReminderScheduler: CommissionReminderScheduler,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    @Transactional
    fun execute(
        userId: String,
        commissionId: Long,
        topicId: Long,
        request: SelectTopicRequest,
    ): CommissionTopicResponse {
        if (!request.isSelected) {
            throw BusinessException(GlobalErrorCode.INVALID_INPUT)
        }

        val commission = commissionAdaptor.findById(commissionId)

        if (commission.studentUserId != userId) {
            throw BusinessException(CommissionErrorCode.UNAUTHORIZED_ACCESS)
        }

        val topic = commissionTopicAdaptor.findById(topicId)

        if (topic.commission.id != commissionId) {
            throw BusinessException(CommissionErrorCode.COMMISSION_TOPIC_NOT_FOUND)
        }

        if (commission.status != CommissionStatus.TOPIC_PROPOSED) {
            throw BusinessException(CommissionErrorCode.INVALID_STATUS_TRANSITION)
        }

        val membershipEnrollment =
            enrollmentAdaptor.findActiveMembershipEnrollment(
                studentUserId = commission.studentUserId,
                now = LocalDateTime.now(clock),
            )
                ?: throw NoActiveMembershipException()

        val lesson =
            lessonAdaptor.save(
                Lesson(
                    lessonType = LessonType.COMMISSION,
                    enrollmentId = membershipEnrollment.id,
                    sourceCommissionId = commission.id,
                    studentUserId = commission.studentUserId,
                    assignedTeacherUserId = commission.teacherUserId,
                    name = commission.guideInfo.subject,
                ),
            )

        commission.selectTopicAndAccept(topicId = topic.id, lessonId = lesson.id)
        topic.select()

        commissionReminderScheduler.cancelAllReminders(commissionId)

        return CommissionTopicResponse.from(topic)
    }
}
