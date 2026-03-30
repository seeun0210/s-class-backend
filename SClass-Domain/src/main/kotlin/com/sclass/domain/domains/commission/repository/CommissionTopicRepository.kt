package com.sclass.domain.domains.commission.repository

import com.sclass.domain.domains.commission.domain.CommissionTopic
import org.springframework.data.jpa.repository.JpaRepository

interface CommissionTopicRepository : JpaRepository<CommissionTopic, Long> {
    fun findByCommissionId(commissionId: Long): List<CommissionTopic>
}
