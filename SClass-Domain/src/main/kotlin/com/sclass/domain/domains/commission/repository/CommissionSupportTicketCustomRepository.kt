package com.sclass.domain.domains.commission.repository

import com.sclass.domain.domains.commission.domain.TicketStatus
import com.sclass.domain.domains.commission.dto.SupportTicketWithUsers

interface CommissionSupportTicketCustomRepository {
    fun findByStatusWithUsers(status: TicketStatus): List<SupportTicketWithUsers>
}
