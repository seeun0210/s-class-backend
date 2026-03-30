package com.sclass.supporters.commission.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.common.exception.BusinessException
import com.sclass.domain.domains.commission.adaptor.CommissionAdaptor
import com.sclass.domain.domains.commission.adaptor.CommissionTopicAdaptor
import com.sclass.domain.domains.commission.domain.CommissionTopic
import com.sclass.domain.domains.commission.exception.CommissionErrorCode
import com.sclass.supporters.commission.dto.CommissionTopicListResponse
import com.sclass.supporters.commission.dto.ProposeTopicsRequest
import org.springframework.transaction.annotation.Transactional

@UseCase
class ProposeTopicsUseCase(
    private val commissionAdaptor: CommissionAdaptor,
    private val commissionTopicAdaptor: CommissionTopicAdaptor,
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

        return CommissionTopicListResponse.from(topics)
    }
}
