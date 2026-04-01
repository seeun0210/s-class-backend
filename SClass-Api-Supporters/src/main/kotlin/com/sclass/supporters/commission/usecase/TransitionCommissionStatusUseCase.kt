package com.sclass.supporters.commission.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.common.exception.BusinessException
import com.sclass.common.exception.GlobalErrorCode
import com.sclass.domain.domains.commission.adaptor.CommissionAdaptor
import com.sclass.domain.domains.commission.adaptor.MessageAdaptor
import com.sclass.domain.domains.commission.domain.Commission
import com.sclass.domain.domains.commission.domain.CommissionStatus
import com.sclass.domain.domains.commission.domain.Message
import com.sclass.domain.domains.commission.domain.MessageType
import com.sclass.domain.domains.commission.exception.CommissionErrorCode
import com.sclass.supporters.commission.dto.CommissionResponse
import com.sclass.supporters.commission.dto.TransitionStatusRequest
import com.sclass.supporters.commission.scheduler.CommissionReminderScheduler
import org.springframework.transaction.annotation.Transactional

@UseCase
class TransitionCommissionStatusUseCase(
    private val commissionAdaptor: CommissionAdaptor,
    private val messageAdaptor: MessageAdaptor,
    private val commissionReminderScheduler: CommissionReminderScheduler,
) {
    @Transactional
    fun execute(
        userId: String,
        commissionId: Long,
        request: TransitionStatusRequest,
    ): CommissionResponse {
        val commission = commissionAdaptor.findById(commissionId)

        when (request.status) {
            CommissionStatus.IN_PROGRESS -> {
                validateTeacher(commission, userId)
                commission.start()
                commissionReminderScheduler.resetInactiveReminder(commissionId)
            }
            CommissionStatus.COMPLETED -> {
                validateTeacher(commission, userId)
                commission.complete()
                commissionReminderScheduler.cancelAllReminders(commissionId)
            }
            CommissionStatus.REJECTED -> {
                validateTeacher(commission, userId)
                commission.reject()
                commissionReminderScheduler.cancelAllReminders(commissionId)
                request.reason?.takeIf { it.isNotBlank() }?.let {
                    messageAdaptor.save(
                        Message(
                            commission = commission,
                            senderId = userId,
                            type = MessageType.REJECTION_REASON,
                            content = it,
                        ),
                    )
                }
            }
            CommissionStatus.CANCELLED -> {
                validateStudent(commission, userId)
                commission.cancel()
                commissionReminderScheduler.cancelAllReminders(commissionId)
            }
            else -> throw BusinessException(GlobalErrorCode.INVALID_INPUT)
        }

        return CommissionResponse.from(commission)
    }

    private fun validateTeacher(
        commission: Commission,
        userId: String,
    ) {
        if (commission.teacherUserId != userId) {
            throw BusinessException(CommissionErrorCode.UNAUTHORIZED_ACCESS)
        }
    }

    private fun validateStudent(
        commission: Commission,
        userId: String,
    ) {
        if (commission.studentUserId != userId) {
            throw BusinessException(CommissionErrorCode.UNAUTHORIZED_ACCESS)
        }
    }
}
