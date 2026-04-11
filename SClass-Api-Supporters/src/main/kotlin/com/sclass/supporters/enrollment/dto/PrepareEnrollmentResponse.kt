package com.sclass.supporters.enrollment.dto

data class PrepareEnrollmentResponse(
    val paymentId: String,
    val pgOrderId: String,
    val amount: Int,
    val courseId: Long,
    val courseName: String,
)
