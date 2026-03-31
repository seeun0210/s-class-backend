package com.sclass.supporters.commission.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.common.exception.BusinessException
import com.sclass.domain.domains.commission.adaptor.CommissionAdaptor
import com.sclass.domain.domains.commission.adaptor.CommissionSupportTicketAdaptor
import com.sclass.domain.domains.commission.exception.CommissionErrorCode
import com.sclass.supporters.commission.dto.SupportTicketListResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetSupportTicketsUseCase(
    private val commissionAdaptor: CommissionAdaptor,
    private val commissionSupportTicketAdaptor: CommissionSupportTicketAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(
        userId: String,
        commissionId: Long,
    ): SupportTicketListResponse {
        val commission = commissionAdaptor.findById(commissionId)

        if (commission.teacherUserId != userId && commission.studentUserId != userId) {
            throw BusinessException(CommissionErrorCode.UNAUTHORIZED_ACCESS)
        }

        return SupportTicketListResponse.from(
            commissionSupportTicketAdaptor.findByCommissionId(commissionId),
        )
    }
}
