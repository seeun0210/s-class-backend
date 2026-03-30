package com.sclass.supporters.commission.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.common.exception.BusinessException
import com.sclass.domain.domains.commission.adaptor.CommissionAdaptor
import com.sclass.domain.domains.commission.exception.CommissionErrorCode
import com.sclass.supporters.commission.dto.CommissionResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetCommissionDetailUseCase(
    private val commissionAdaptor: CommissionAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(
        userId: String,
        commissionId: Long,
    ): CommissionResponse {
        val commission = commissionAdaptor.findById(commissionId)

        if (commission.studentUserId != userId && commission.teacherUserId != userId) {
            throw BusinessException(CommissionErrorCode.UNAUTHORIZED_ACCESS)
        }

        return CommissionResponse.from(commission)
    }
}
