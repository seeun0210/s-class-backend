package com.sclass.supporters.commission.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.common.exception.BusinessException
import com.sclass.domain.domains.commission.adaptor.CommissionAdaptor
import com.sclass.domain.domains.commission.adaptor.MessageAdaptor
import com.sclass.domain.domains.commission.domain.Message
import com.sclass.domain.domains.commission.exception.CommissionErrorCode
import com.sclass.supporters.commission.dto.MessageResponse
import com.sclass.supporters.commission.dto.SendMessageRequest
import org.springframework.transaction.annotation.Transactional

@UseCase
class SendMessageUseCase(
    private val commissionAdaptor: CommissionAdaptor,
    private val messageAdaptor: MessageAdaptor,
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

        if (isTeacher) {
            commission.requestAdditionalInfo()
        } else {
            commission.resubmit()
        }

        val message =
            messageAdaptor.save(
                Message(
                    commission = commission,
                    senderId = userId,
                    content = request.content,
                ),
            )

        return MessageResponse.from(message)
    }
}
