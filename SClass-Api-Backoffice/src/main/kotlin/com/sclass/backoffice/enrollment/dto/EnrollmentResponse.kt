package com.sclass.backoffice.enrollment.dto

import com.sclass.domain.domains.enrollment.domain.Enrollment
import com.sclass.domain.domains.enrollment.domain.EnrollmentStatus
import com.sclass.domain.domains.enrollment.domain.EnrollmentType

data class EnrollmentResponse(
    val id: Long,
    val courseId: Long,
    val studentUserId: String,
    val enrollmentType: EnrollmentType,
    val tuitionAmountWon: Int,
    val status: EnrollmentStatus,
) {
    companion object {
        fun from(enrollment: Enrollment) =
            EnrollmentResponse(
                id = enrollment.id,
                courseId = enrollment.courseId,
                studentUserId = enrollment.studentUserId,
                enrollmentType = enrollment.enrollmentType,
                tuitionAmountWon = enrollment.tuitionAmountWon,
                status = enrollment.status,
            )
    }
}
