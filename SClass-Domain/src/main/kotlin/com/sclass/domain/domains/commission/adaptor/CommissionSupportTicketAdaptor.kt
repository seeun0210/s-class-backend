package com.sclass.domain.domains.commission.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.commission.domain.CommissionSupportTicket
import com.sclass.domain.domains.commission.domain.TicketStatus
import com.sclass.domain.domains.commission.exception.CommissionSupportTicketNotFoundException
import com.sclass.domain.domains.commission.repository.CommissionSupportTicketRepository

@Adaptor
class CommissionSupportTicketAdaptor(
    private val commissionSupportTicketRepository: CommissionSupportTicketRepository,
) {
    fun save(ticket: CommissionSupportTicket): CommissionSupportTicket = commissionSupportTicketRepository.save(ticket)

    fun findById(id: Long): CommissionSupportTicket =
        commissionSupportTicketRepository.findById(id).orElseThrow { CommissionSupportTicketNotFoundException() }

    fun findByCommissionId(commissionId: Long): List<CommissionSupportTicket> =
        commissionSupportTicketRepository.findByCommissionId(commissionId)

    fun findByStatus(status: TicketStatus): List<CommissionSupportTicket> = commissionSupportTicketRepository.findByStatus(status)
}
