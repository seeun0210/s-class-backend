package com.sclass.backoffice.supportticket.usecase

import com.sclass.backoffice.supportticket.dto.SupportTicketDetailResponse
import com.sclass.backoffice.supportticket.dto.SupportTicketListResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.commission.adaptor.CommissionSupportTicketAdaptor
import com.sclass.domain.domains.commission.domain.TicketStatus
import com.sclass.domain.domains.student.adaptor.StudentAdaptor
import com.sclass.domain.domains.student.domain.StudentDocumentType
import com.sclass.domain.domains.user.adaptor.UserAdaptor
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetOpenSupportTicketsUseCase(
    private val commissionSupportTicketAdaptor: CommissionSupportTicketAdaptor,
    private val userAdaptor: UserAdaptor,
    private val studentAdaptor: StudentAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(): SupportTicketListResponse {
        val tickets = commissionSupportTicketAdaptor.findByStatus(TicketStatus.OPEN)

        val responses =
            tickets.map { ticket ->
                val teacherUser = userAdaptor.findById(ticket.commission.teacherUserId)
                val student = studentAdaptor.findByUserIdWithUser(ticket.commission.studentUserId)
                val transcript =
                    studentAdaptor
                        .findDocumentsWithFileByStudentId(student.id)
                        .firstOrNull { it.documentType == StudentDocumentType.TRANSCRIPT }
                SupportTicketDetailResponse.from(ticket, teacherUser, student, transcript)
            }

        return SupportTicketListResponse(tickets = responses)
    }
}
