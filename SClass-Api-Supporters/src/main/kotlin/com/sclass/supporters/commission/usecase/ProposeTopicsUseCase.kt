package com.sclass.supporters.commission.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.common.exception.BusinessException
import com.sclass.domain.domains.commission.adaptor.CommissionAdaptor
import com.sclass.domain.domains.commission.adaptor.CommissionTopicAdaptor
import com.sclass.domain.domains.commission.domain.CommissionTopic
import com.sclass.domain.domains.commission.exception.CommissionErrorCode
import com.sclass.supporters.commission.dto.CommissionTopicListResponse
import com.sclass.supporters.commission.dto.ProposeTopicsRequest
import com.sclass.supporters.commission.event.TopicSuggestedEvent
import com.sclass.supporters.commission.scheduler.CommissionReminderScheduler
import org.springframework.context.ApplicationEventPublisher
import org.springframework.transaction.annotation.Transactional

@UseCase
class ProposeTopicsUseCase(
    private val commissionAdaptor: CommissionAdaptor,
    private val commissionTopicAdaptor: CommissionTopicAdaptor,
    private val eventPublisher: ApplicationEventPublisher,
    private val commissionReminderScheduler: CommissionReminderScheduler,
) {
    @Transactional
    fun execute(
        userId: String,
        commissionId: Long,
        request: ProposeTopicsRequest,
    ): CommissionTopicListResponse {
        val commission = commissionAdaptor.findById(commissionId)

        if (commission.teacherUserId != userId) {
            throw BusinessException(CommissionErrorCode.UNAUTHORIZED_ACCESS)
        }

        commission.proposeTopics()

        val topics =
            commissionTopicAdaptor.saveAll(
                request.topics.map {
                    CommissionTopic(
                        commission = commission,
                        topicId = it.topicId,
                        title = it.title,
                        description = it.description,
                    )
                },
            )

        commissionReminderScheduler.cancelNoRespReminders(commissionId)
        commissionReminderScheduler.resetInactiveReminder(commissionId)

        eventPublisher.publishEvent(
            TopicSuggestedEvent(
                studentUserId = commission.studentUserId,
                commissionId = commissionId.toString(),
            ),
        )

        return CommissionTopicListResponse.from(topics)
    }
}
