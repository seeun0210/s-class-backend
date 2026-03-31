package com.sclass.backoffice.supportticket.dto

import com.sclass.domain.domains.commission.domain.CommissionSupportTicket
import com.sclass.domain.domains.commission.domain.SupportTicketType
import com.sclass.domain.domains.commission.domain.TicketStatus
import com.sclass.domain.domains.user.domain.User
import java.time.LocalDateTime

data class SupportTicketListResponse(
    val tickets: List<SupportTicketSummaryResponse>,
)

data class SupportTicketSummaryResponse(
    val id: Long,
    val commissionId: Long,
    val type: SupportTicketType,
    val reason: String,
    val status: TicketStatus,
    val teacher: TeacherSummary,
    val student: StudentSummary,
    val createdAt: LocalDateTime?,
) {
    companion object {
        fun from(
            ticket: CommissionSupportTicket,
            teacherUser: User,
            studentUser: User,
        ) = SupportTicketSummaryResponse(
            id = ticket.id,
            commissionId = ticket.commission.id,
            type = ticket.type,
            reason = ticket.reason,
            status = ticket.status,
            teacher = TeacherSummary.from(teacherUser),
            student = StudentSummary.from(studentUser),
            createdAt = ticket.createdAt,
        )
    }
}

data class StudentSummary(
    val userId: String,
    val name: String,
    val email: String,
) {
    companion object {
        fun from(user: User) =
            StudentSummary(
                userId = user.id,
                name = user.name,
                email = user.email,
            )
    }
}
