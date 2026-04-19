package com.sclass.backoffice.enrollment.dto

import com.sclass.domain.domains.enrollment.domain.Enrollment
import com.sclass.domain.domains.enrollment.domain.EnrollmentStatus
import com.sclass.domain.domains.enrollment.domain.EnrollmentType
import java.time.LocalDateTime

data class EnrollmentResponse(
    val id: Long,
    val courseId: Long?,
    val productId: String?,
    val studentUserId: String,
    val enrollmentType: EnrollmentType,
    val tuitionAmountWon: Int,
    val status: EnrollmentStatus,
    val startAt: LocalDateTime?,
    val endAt: LocalDateTime?,
) {
    companion object {
        fun from(enrollment: Enrollment) =
            EnrollmentResponse(
                id = enrollment.id,
                courseId = enrollment.courseId,
                productId = enrollment.productId,
                studentUserId = enrollment.studentUserId,
                enrollmentType = enrollment.enrollmentType,
                tuitionAmountWon = enrollment.tuitionAmountWon,
                status = enrollment.status,
                startAt = enrollment.startAt,
                endAt = enrollment.endAt,
            )
    }
}
