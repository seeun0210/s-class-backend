package com.sclass.domain.domains.commission.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.commission.domain.CommissionPolicy
import com.sclass.domain.domains.commission.exception.CommissionPolicyNotConfiguredException
import com.sclass.domain.domains.commission.exception.CommissionPolicyNotFoundException
import com.sclass.domain.domains.commission.repository.CommissionPolicyRepository

@Adaptor
class CommissionPolicyAdaptor(
    private val commissionPolicyRepository: CommissionPolicyRepository,
) {
    fun findById(id: String): CommissionPolicy = commissionPolicyRepository.findById(id).orElseThrow { CommissionPolicyNotFoundException() }

    fun findActive(): CommissionPolicy =
        commissionPolicyRepository.findFirstByActiveTrueOrderByCreatedAtDesc()
            ?: throw CommissionPolicyNotConfiguredException()

    fun findActiveOrNull(): CommissionPolicy? = commissionPolicyRepository.findFirstByActiveTrueOrderByCreatedAtDesc()

    fun findAll(): List<CommissionPolicy> = commissionPolicyRepository.findAll()

    fun save(policy: CommissionPolicy): CommissionPolicy = commissionPolicyRepository.save(policy)
}
