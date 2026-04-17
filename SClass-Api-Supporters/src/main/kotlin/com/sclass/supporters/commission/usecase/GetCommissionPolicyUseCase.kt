package com.sclass.supporters.commission.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.commission.adaptor.CommissionPolicyAdaptor
import com.sclass.supporters.commission.dto.CommissionPolicyResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetCommissionPolicyUseCase(
    private val commissionPolicyAdaptor: CommissionPolicyAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(): CommissionPolicyResponse = CommissionPolicyResponse.from(commissionPolicyAdaptor.findActive())
}
