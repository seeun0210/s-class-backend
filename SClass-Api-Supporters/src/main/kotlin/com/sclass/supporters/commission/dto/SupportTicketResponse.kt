package com.sclass.supporters.commission.dto

import com.sclass.domain.domains.commission.domain.CommissionSupportTicket
import com.sclass.domain.domains.commission.domain.SupportTicketType
import com.sclass.domain.domains.commission.domain.TicketStatus
import java.time.LocalDateTime

data class SupportTicketResponse(
    val id: Long,
    val commissionId: Long,
    val type: SupportTicketType,
    val reason: String,
    val status: TicketStatus,
    val response: String?,
    val createdAt: LocalDateTime?,
) {
    companion object {
        fun from(ticket: CommissionSupportTicket) =
            SupportTicketResponse(
                id = ticket.id,
                commissionId = ticket.commission.id,
                type = ticket.type,
                reason = ticket.reason,
                status = ticket.status,
                response = ticket.response,
                createdAt = ticket.createdAt,
            )
    }
}
