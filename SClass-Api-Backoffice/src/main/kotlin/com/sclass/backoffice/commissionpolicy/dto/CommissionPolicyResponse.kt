package com.sclass.backoffice.commissionpolicy.dto

import com.sclass.domain.domains.commission.domain.CommissionPolicy

data class CommissionPolicyResponse(
    val id: String,
    val name: String,
    val coinCost: Int,
    val active: Boolean,
) {
    companion object {
        fun from(policy: CommissionPolicy) =
            CommissionPolicyResponse(
                id = policy.id,
                name = policy.name,
                coinCost = policy.coinCost,
                active = policy.active,
            )
    }
}
