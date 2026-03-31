package com.sclass.backoffice.supportticket.usecase

import com.sclass.backoffice.supportticket.dto.SupportTicketListResponse
import com.sclass.backoffice.supportticket.dto.SupportTicketSummaryResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.commission.adaptor.CommissionSupportTicketAdaptor
import com.sclass.domain.domains.commission.domain.TicketStatus
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetOpenSupportTicketsUseCase(
    private val commissionSupportTicketAdaptor: CommissionSupportTicketAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(status: TicketStatus): SupportTicketListResponse {
        val ticketsWithUsers = commissionSupportTicketAdaptor.findByStatusWithUsers(status)

        val responses =
            ticketsWithUsers.map {
                SupportTicketSummaryResponse.from(
                    ticket = it.ticket,
                    teacherUser = it.teacherUser,
                    studentUser = it.studentUser,
                )
            }

        return SupportTicketListResponse(tickets = responses)
    }
}
