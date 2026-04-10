package com.sclass.supporters.commission.event

data class CommissionAssignedEvent(
    val teacherUserId: String,
    val studentUserId: String,
    val commissionId: String,
    val subject: String,
    val createdAt: String,
)

data class TopicSuggestedEvent(
    val studentUserId: String,
    val commissionId: String,
)
