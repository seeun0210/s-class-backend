package com.sclass.domain.domains.commission.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.commission.domain.CommissionFile
import com.sclass.domain.domains.commission.repository.CommissionFileRepository

@Adaptor
class CommissionFileAdaptor(
    private val commissionFileRepository: CommissionFileRepository,
) {
    fun findByCommissionId(commissionId: Long): List<CommissionFile> = commissionFileRepository.findByCommissionId(commissionId)

    fun saveAll(files: List<CommissionFile>): List<CommissionFile> = commissionFileRepository.saveAll(files)
}
