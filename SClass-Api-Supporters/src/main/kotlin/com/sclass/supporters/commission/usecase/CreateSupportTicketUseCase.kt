package com.sclass.supporters.commission.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.common.exception.BusinessException
import com.sclass.domain.domains.commission.adaptor.CommissionAdaptor
import com.sclass.domain.domains.commission.adaptor.CommissionSupportTicketAdaptor
import com.sclass.domain.domains.commission.domain.CommissionSupportTicket
import com.sclass.domain.domains.commission.exception.CommissionErrorCode
import com.sclass.supporters.commission.dto.CreateSupportTicketRequest
import com.sclass.supporters.commission.dto.SupportTicketResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class CreateSupportTicketUseCase(
    private val commissionAdaptor: CommissionAdaptor,
    private val commissionSupportTicketAdaptor: CommissionSupportTicketAdaptor,
) {
    @Transactional
    fun execute(
        userId: String,
        commissionId: Long,
        request: CreateSupportTicketRequest,
    ): SupportTicketResponse {
        val commission = commissionAdaptor.findById(commissionId)

        if (commission.teacherUserId != userId) {
            throw BusinessException(CommissionErrorCode.UNAUTHORIZED_ACCESS)
        }

        val ticket =
            commissionSupportTicketAdaptor.save(
                CommissionSupportTicket(
                    commission = commission,
                    type = request.type,
                    reason = request.reason,
                ),
            )

        return SupportTicketResponse.from(ticket)
    }
}
