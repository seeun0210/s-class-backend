package com.sclass.domain.domains.commission.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.common.exception.BusinessException
import com.sclass.domain.domains.commission.domain.CommissionFile
import com.sclass.domain.domains.commission.exception.CommissionErrorCode
import com.sclass.domain.domains.commission.repository.CommissionFileRepository
import org.springframework.data.repository.findByIdOrNull

@Adaptor
class CommissionFileAdaptor(
    private val commissionFileRepository: CommissionFileRepository,
) {
    fun findByCommissionId(commissionId: Long): List<CommissionFile> = commissionFileRepository.findByCommissionId(commissionId)

    fun findById(id: Long): CommissionFile =
        commissionFileRepository.findByIdOrNull(id)
            ?: throw BusinessException(CommissionErrorCode.COMMISSION_FILE_NOT_FOUND)

    fun saveAll(files: List<CommissionFile>): List<CommissionFile> = commissionFileRepository.saveAll(files)

    fun delete(file: CommissionFile) {
        commissionFileRepository.delete(file)
    }

    fun deleteAll(files: List<CommissionFile>) {
        commissionFileRepository.deleteAll(files)
    }
}
