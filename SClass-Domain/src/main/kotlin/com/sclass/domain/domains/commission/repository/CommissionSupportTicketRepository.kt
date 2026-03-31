package com.sclass.domain.domains.commission.repository

import com.sclass.domain.domains.commission.domain.CommissionSupportTicket
import com.sclass.domain.domains.commission.domain.TicketStatus
import org.springframework.data.jpa.repository.JpaRepository

interface CommissionSupportTicketRepository : JpaRepository<CommissionSupportTicket, Long> {
    fun findByCommissionId(commissionId: Long): List<CommissionSupportTicket>

    fun findByStatus(status: TicketStatus): List<CommissionSupportTicket>
}
