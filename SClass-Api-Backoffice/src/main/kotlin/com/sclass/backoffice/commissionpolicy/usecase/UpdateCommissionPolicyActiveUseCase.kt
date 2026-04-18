package com.sclass.backoffice.commissionpolicy.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.commission.adaptor.CommissionPolicyAdaptor
import org.springframework.transaction.annotation.Transactional

@UseCase
class UpdateCommissionPolicyActiveUseCase(
    private val commissionPolicyAdaptor: CommissionPolicyAdaptor,
) {
    @Transactional
    fun execute(
        id: String,
        active: Boolean,
    ) {
        val policy = commissionPolicyAdaptor.findById(id)
        policy.active = active
    }
}
