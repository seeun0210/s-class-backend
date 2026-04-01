package com.sclass.supporters.commission.dto

import com.sclass.domain.domains.commission.domain.CommissionSupportTicket

data class SupportTicketListResponse(
    val tickets: List<SupportTicketResponse>,
) {
    companion object {
        fun from(tickets: List<CommissionSupportTicket>) =
            SupportTicketListResponse(
                tickets = tickets.map { SupportTicketResponse.from(it) },
            )
    }
}
