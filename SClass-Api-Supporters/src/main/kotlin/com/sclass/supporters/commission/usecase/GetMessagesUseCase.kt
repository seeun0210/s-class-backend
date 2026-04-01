package com.sclass.supporters.commission.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.common.exception.BusinessException
import com.sclass.domain.domains.commission.adaptor.CommissionAdaptor
import com.sclass.domain.domains.commission.adaptor.MessageAdaptor
import com.sclass.domain.domains.commission.exception.CommissionErrorCode
import com.sclass.supporters.commission.dto.MessageListResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetMessagesUseCase(
    private val commissionAdaptor: CommissionAdaptor,
    private val messageAdaptor: MessageAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(
        userId: String,
        commissionId: Long,
    ): MessageListResponse {
        val commission = commissionAdaptor.findById(commissionId)

        if (commission.studentUserId != userId && commission.teacherUserId != userId) {
            throw BusinessException(CommissionErrorCode.UNAUTHORIZED_ACCESS)
        }

        return MessageListResponse.from(messageAdaptor.findByCommissionId(commissionId))
    }
}
