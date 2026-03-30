package com.sclass.domain.domains.commission.repository

import com.sclass.domain.domains.commission.domain.CommissionFile
import org.springframework.data.jpa.repository.JpaRepository

interface CommissionFileRepository : JpaRepository<CommissionFile, Long> {
    fun findByCommissionId(commissionId: Long): List<CommissionFile>
}
