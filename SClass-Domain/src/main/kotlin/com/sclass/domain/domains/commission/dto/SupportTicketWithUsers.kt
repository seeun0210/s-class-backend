package com.sclass.domain.domains.commission.dto

import com.sclass.domain.domains.commission.domain.CommissionSupportTicket
import com.sclass.domain.domains.user.domain.User

data class SupportTicketWithUsers(
    val ticket: CommissionSupportTicket,
    val teacherUser: User,
    val studentUser: User,
)
