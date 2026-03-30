package com.sclass.supporters.commission.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.common.exception.BusinessException
import com.sclass.domain.domains.commission.adaptor.CommissionAdaptor
import com.sclass.domain.domains.commission.adaptor.CommissionTopicAdaptor
import com.sclass.domain.domains.commission.exception.CommissionErrorCode
import com.sclass.supporters.commission.dto.CommissionTopicListResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetCommissionTopicsUseCase(
    private val commissionAdaptor: CommissionAdaptor,
    private val commissionTopicAdaptor: CommissionTopicAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(
        userId: String,
        commissionId: Long,
    ): CommissionTopicListResponse {
        val commission = commissionAdaptor.findById(commissionId)

        if (commission.studentUserId != userId && commission.teacherUserId != userId) {
            throw BusinessException(CommissionErrorCode.UNAUTHORIZED_ACCESS)
        }

        val topics = commissionTopicAdaptor.findByCommissionId(commissionId)
        return CommissionTopicListResponse.from(topics)
    }
}
