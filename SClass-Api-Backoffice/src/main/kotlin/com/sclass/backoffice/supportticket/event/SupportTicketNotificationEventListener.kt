package com.sclass.backoffice.supportticket.event

import com.sclass.common.annotation.EventHandler
import com.sclass.domain.domains.user.adaptor.UserAdaptor
import com.sclass.infrastructure.message.CommissionNotificationSender
import org.springframework.scheduling.annotation.Async
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@EventHandler
class SupportTicketNotificationEventListener(
    private val userAdaptor: UserAdaptor,
    private val commissionNotificationSender: CommissionNotificationSender,
) {
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: TicketResolvedEvent) {
        val teacher = userAdaptor.findById(event.teacherUserId)
        val phoneNumber = teacher.phoneNumber ?: return
        commissionNotificationSender.sendTicketResolved(
            phoneNumber = phoneNumber,
            teacherName = teacher.name,
            ticketType = event.ticketType,
            commissionId = event.commissionId,
        )
    }
}
