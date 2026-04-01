package com.sclass.infrastructure.message

interface CommissionNotificationSender {
    fun sendCommissionAssigned(
        phoneNumber: String,
        teacherName: String,
        studentName: String,
        subject: String,
        createdAt: String,
        commissionId: String,
    )

    fun sendTopicSuggested(
        phoneNumber: String,
        studentName: String,
        commissionId: String,
    )

    fun sendAdditionalInfoRequested(
        phoneNumber: String,
        studentName: String,
        requestContent: String,
        commissionId: String,
    )

    fun sendTicketResolved(
        phoneNumber: String,
        teacherName: String,
        ticketType: String,
        commissionId: String,
    )

    fun sendNoResponseReminder(
        phoneNumber: String,
        teacherName: String,
        studentName: String,
        elapsedTime: String,
        commissionId: String,
    )

    fun sendInactivityReminder(
        phoneNumber: String,
        teacherName: String,
        studentName: String,
        inactiveDays: Int,
        lastActivityAt: String,
        commissionId: String,
    )
}
