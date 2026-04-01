package com.sclass.supporters.commission.event

import com.sclass.common.annotation.EventHandler
import com.sclass.domain.domains.user.adaptor.UserAdaptor
import com.sclass.infrastructure.message.CommissionNotificationSender
import org.springframework.scheduling.annotation.Async
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@EventHandler
class CommissionNotificationEventListener(
    private val userAdaptor: UserAdaptor,
    private val commissionNotificationSender: CommissionNotificationSender,
) {
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: CommissionAssignedEvent) {
        val teacher = userAdaptor.findById(event.teacherUserId)
        val student = userAdaptor.findById(event.studentUserId)
        val phoneNumber = teacher.phoneNumber ?: return
        commissionNotificationSender.sendCommissionAssigned(
            phoneNumber = phoneNumber,
            teacherName = teacher.name,
            studentName = student.name,
            subject = event.subject,
            createdAt = event.createdAt,
            commissionId = event.commissionId,
        )
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: TopicSuggestedEvent) {
        val student = userAdaptor.findById(event.studentUserId)
        val phoneNumber = student.phoneNumber ?: return
        commissionNotificationSender.sendTopicSuggested(
            phoneNumber = phoneNumber,
            studentName = student.name,
            commissionId = event.commissionId,
        )
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: AdditionalInfoRequestedEvent) {
        val student = userAdaptor.findById(event.studentUserId)
        val phoneNumber = student.phoneNumber ?: return
        commissionNotificationSender.sendAdditionalInfoRequested(
            phoneNumber = phoneNumber,
            studentName = student.name,
            requestContent = event.requestContent,
            commissionId = event.commissionId,
        )
    }
}
