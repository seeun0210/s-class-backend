package com.sclass.domain.domains.commission.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.commission.domain.CommissionPolicy
import com.sclass.domain.domains.commission.exception.CommissionPolicyNotConfiguredException
import com.sclass.domain.domains.commission.repository.CommissionPolicyRepository

@Adaptor
class CommissionPolicyAdaptor(
    private val commissionPolicyRepository: CommissionPolicyRepository,
) {
    fun findActive(): CommissionPolicy =
        commissionPolicyRepository.findFirstByActiveTrueOrderByCreatedAtDesc()
            ?: throw CommissionPolicyNotConfiguredException()

    fun findActiveOrNull(): CommissionPolicy? = commissionPolicyRepository.findFirstByActiveTrueOrderByCreatedAtDesc()

    fun save(policy: CommissionPolicy): CommissionPolicy = commissionPolicyRepository.save(policy)
}
