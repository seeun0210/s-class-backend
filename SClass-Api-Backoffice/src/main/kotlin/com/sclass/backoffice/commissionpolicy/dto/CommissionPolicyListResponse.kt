package com.sclass.backoffice.commissionpolicy.dto

import com.sclass.domain.domains.commission.domain.CommissionPolicy

data class CommissionPolicyListResponse(
    val commissionPolicies: List<CommissionPolicyResponse>,
) {
    companion object {
        fun from(policies: List<CommissionPolicy>) = CommissionPolicyListResponse(policies.map { CommissionPolicyResponse.from(it) })
    }
}
