package com.sclass.supporters.commission.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.common.exception.BusinessException
import com.sclass.domain.domains.commission.adaptor.CommissionAdaptor
import com.sclass.domain.domains.commission.adaptor.CommissionTopicAdaptor
import com.sclass.domain.domains.commission.exception.CommissionErrorCode
import com.sclass.supporters.commission.dto.CommissionTopicResponse
import com.sclass.supporters.commission.dto.SelectTopicRequest
import org.springframework.transaction.annotation.Transactional

@UseCase
class SelectTopicUseCase(
    private val commissionAdaptor: CommissionAdaptor,
    private val commissionTopicAdaptor: CommissionTopicAdaptor,
) {
    @Transactional
    fun execute(
        userId: String,
        commissionId: Long,
        topicId: Long,
        request: SelectTopicRequest,
    ): CommissionTopicResponse {
        if (!request.isSelected) {
            throw BusinessException(CommissionErrorCode.INVALID_STATUS_TRANSITION)
        }

        val commission = commissionAdaptor.findById(commissionId)

        if (commission.studentUserId != userId) {
            throw BusinessException(CommissionErrorCode.UNAUTHORIZED_ACCESS)
        }

        val topic = commissionTopicAdaptor.findById(topicId)

        if (topic.commission.id != commissionId) {
            throw BusinessException(CommissionErrorCode.COMMISSION_TOPIC_NOT_FOUND)
        }

        commission.selectTopic()
        topic.select()

        return CommissionTopicResponse.from(topic)
    }
}
