package com.sclass.domain.domains.commission.repository

import com.sclass.domain.domains.commission.domain.CommissionPolicy
import org.springframework.data.jpa.repository.JpaRepository

interface CommissionPolicyRepository : JpaRepository<CommissionPolicy, String> {
    fun findFirstByActiveTrueOrderByCreatedAtDesc(): CommissionPolicy?
}
