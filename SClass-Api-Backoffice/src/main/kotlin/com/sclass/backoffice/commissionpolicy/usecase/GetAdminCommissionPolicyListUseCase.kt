package com.sclass.backoffice.commissionpolicy.usecase

import com.sclass.backoffice.commissionpolicy.dto.CommissionPolicyListResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.commission.adaptor.CommissionPolicyAdaptor
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetAdminCommissionPolicyListUseCase(
    private val commissionPolicyAdaptor: CommissionPolicyAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(): CommissionPolicyListResponse = CommissionPolicyListResponse.from(commissionPolicyAdaptor.findAll())
}
