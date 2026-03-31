package com.sclass.backoffice.supportticket.usecase

import com.sclass.backoffice.supportticket.dto.ResolveSupportTicketRequest
import com.sclass.backoffice.supportticket.dto.SupportTicketDetailResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.commission.adaptor.CommissionSupportTicketAdaptor
import com.sclass.domain.domains.student.adaptor.StudentAdaptor
import com.sclass.domain.domains.student.domain.StudentDocumentType
import com.sclass.domain.domains.user.adaptor.UserAdaptor
import org.springframework.transaction.annotation.Transactional

@UseCase
class ResolveSupportTicketUseCase(
    private val commissionSupportTicketAdaptor: CommissionSupportTicketAdaptor,
    private val userAdaptor: UserAdaptor,
    private val studentAdaptor: StudentAdaptor,
) {
    @Transactional
    fun execute(
        ticketId: Long,
        request: ResolveSupportTicketRequest,
    ): SupportTicketDetailResponse {
        val ticket = commissionSupportTicketAdaptor.findById(ticketId)
        ticket.resolve(request.response)

        val teacherUser = userAdaptor.findById(ticket.commission.teacherUserId)
        val student = studentAdaptor.findByUserIdWithUser(ticket.commission.studentUserId)
        val transcript =
            studentAdaptor
                .findDocumentsWithFileByStudentId(student.id)
                .firstOrNull { it.documentType == StudentDocumentType.TRANSCRIPT }

        return SupportTicketDetailResponse.from(ticket, teacherUser, student, transcript)
    }
}
