package com.sclass.supporters.commission.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.common.exception.BusinessException
import com.sclass.common.exception.GlobalErrorCode
import com.sclass.domain.domains.commission.adaptor.CommissionAdaptor
import com.sclass.domain.domains.commission.adaptor.MessageAdaptor
import com.sclass.domain.domains.commission.domain.CommissionStatus
import com.sclass.domain.domains.commission.domain.Message
import com.sclass.domain.domains.commission.domain.MessageType
import com.sclass.domain.domains.commission.exception.CommissionErrorCode
import com.sclass.supporters.commission.dto.MessageResponse
import com.sclass.supporters.commission.dto.SendMessageRequest
import com.sclass.supporters.commission.event.AdditionalInfoRequestedEvent
import com.sclass.supporters.commission.scheduler.CommissionReminderScheduler
import org.springframework.context.ApplicationEventPublisher
import org.springframework.transaction.annotation.Transactional

@UseCase
class SendMessageUseCase(
    private val commissionAdaptor: CommissionAdaptor,
    private val messageAdaptor: MessageAdaptor,
    private val eventPublisher: ApplicationEventPublisher,
    private val commissionReminderScheduler: CommissionReminderScheduler,
) {
    @Transactional
    fun execute(
        userId: String,
        commissionId: Long,
        request: SendMessageRequest,
    ): MessageResponse {
        val commission = commissionAdaptor.findById(commissionId)

        val isTeacher = commission.teacherUserId == userId
        val isStudent = commission.studentUserId == userId

        if (!isTeacher && !isStudent) {
            throw BusinessException(CommissionErrorCode.UNAUTHORIZED_ACCESS)
        }

        validateMessageAllowed(request.type, isTeacher, commission.status)

        when (request.type) {
            MessageType.ADDITIONAL_INFO_REQUEST -> {
                if (isTeacher && commission.status == CommissionStatus.REQUESTED) {
                    commission.requestAdditionalInfo()
                }
            }
            MessageType.ADDITIONAL_INFO_RESPONSE -> {
                if (isStudent && commission.status == CommissionStatus.ADDITIONAL_INFO_REQUESTED) {
                    commission.resubmit()
                }
            }
            else -> throw BusinessException(GlobalErrorCode.INVALID_INPUT)
        }

        val message =
            messageAdaptor.save(
                Message(
                    commission = commission,
                    senderId = userId,
                    type = request.type,
                    content = request.content,
                ),
            )

        if (isTeacher && request.type == MessageType.ADDITIONAL_INFO_REQUEST) {
            commissionReminderScheduler.cancelNoRespReminders(commissionId)
            eventPublisher.publishEvent(
                AdditionalInfoRequestedEvent(
                    studentUserId = commission.studentUserId,
                    requestContent = request.content,
                    commissionId = commissionId.toString(),
                ),
            )
        }

        commissionReminderScheduler.resetInactiveReminder(commissionId)

        return MessageResponse.from(message)
    }

    private fun validateMessageAllowed(
        type: MessageType,
        isTeacher: Boolean,
        status: CommissionStatus,
    ) {
        val allowed =
            when {
                type == MessageType.ADDITIONAL_INFO_REQUEST && isTeacher ->
                    status in setOf(CommissionStatus.REQUESTED, CommissionStatus.TOPIC_SELECTED, CommissionStatus.IN_PROGRESS)

                type == MessageType.ADDITIONAL_INFO_RESPONSE && !isTeacher ->
                    status == CommissionStatus.ADDITIONAL_INFO_REQUESTED

                type == MessageType.ADDITIONAL_INFO_REQUEST && !isTeacher ->
                    status in setOf(CommissionStatus.TOPIC_SELECTED, CommissionStatus.IN_PROGRESS)

                type == MessageType.ADDITIONAL_INFO_RESPONSE && isTeacher ->
                    status in setOf(CommissionStatus.TOPIC_SELECTED, CommissionStatus.IN_PROGRESS)

                else -> false
            }

        if (!allowed) {
            throw BusinessException(GlobalErrorCode.INVALID_INPUT)
        }
    }
}
