package com.sclass.backoffice.commission.usecase

import com.sclass.backoffice.commission.dto.CommissionDetailResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.commission.adaptor.CommissionAdaptor
import com.sclass.domain.domains.commission.adaptor.CommissionFileAdaptor
import com.sclass.domain.domains.commission.adaptor.CommissionTopicAdaptor
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetCommissionDetailUseCase(
    private val commissionAdaptor: CommissionAdaptor,
    private val commissionTopicAdaptor: CommissionTopicAdaptor,
    private val commissionFileAdaptor: CommissionFileAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(commissionId: Long): CommissionDetailResponse {
        val commission = commissionAdaptor.findById(commissionId)
        val topics = commissionTopicAdaptor.findByCommissionId(commission.id)
        val files = commissionFileAdaptor.findByCommissionId(commission.id)

        return CommissionDetailResponse.from(commission, topics, files)
    }
}
