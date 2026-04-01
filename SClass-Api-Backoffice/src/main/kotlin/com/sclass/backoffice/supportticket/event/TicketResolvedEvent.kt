package com.sclass.backoffice.supportticket.event

data class TicketResolvedEvent(
    val teacherUserId: String,
    val ticketType: String,
    val commissionId: String,
)
