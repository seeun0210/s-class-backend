package com.sclass.supporters.commission.dto

import com.sclass.domain.domains.commission.domain.CommissionPolicy

data class CommissionPolicyResponse(
    val commissionPolicyId: String,
    val name: String,
    val coinCost: Int,
) {
    companion object {
        fun from(policy: CommissionPolicy) =
            CommissionPolicyResponse(
                commissionPolicyId = policy.id,
                name = policy.name,
                coinCost = policy.coinCost,
            )
    }
}
