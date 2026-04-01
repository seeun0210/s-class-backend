package com.sclass.domain.domains.commission.repository

import com.sclass.domain.domains.commission.domain.CommissionFile

interface CommissionFileCustomRepository {
    fun findByCommissionId(commissionId: Long): List<CommissionFile>
}
