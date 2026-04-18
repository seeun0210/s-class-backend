package com.sclass.backoffice.commissionpolicy.usecase

import com.sclass.backoffice.commissionpolicy.dto.CommissionPolicyResponse
import com.sclass.backoffice.commissionpolicy.dto.CreateCommissionPolicyRequest
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.commission.adaptor.CommissionPolicyAdaptor
import com.sclass.domain.domains.commission.domain.CommissionPolicy
import org.springframework.transaction.annotation.Transactional

@UseCase
class CreateCommissionPolicyUseCase(
    private val commissionPolicyAdaptor: CommissionPolicyAdaptor,
) {
    @Transactional
    fun execute(request: CreateCommissionPolicyRequest): CommissionPolicyResponse {
        val policy =
            commissionPolicyAdaptor.save(
                CommissionPolicy(
                    name = request.name,
                    coinCost = request.coinCost,
                ),
            )
        return CommissionPolicyResponse.from(policy)
    }
}
