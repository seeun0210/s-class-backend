package com.sclass.supporters.enrollment.dto

import com.sclass.domain.domains.enrollment.domain.Enrollment
import com.sclass.domain.domains.enrollment.domain.EnrollmentStatus
import com.sclass.domain.domains.enrollment.domain.EnrollmentType

data class MyEnrollmentResponse(
    val id: Long,
    val courseId: Long,
    val status: EnrollmentStatus,
    val enrollmentType: EnrollmentType,
    val tuitionAmountWon: Int,
) {
    companion object {
        fun from(enrollment: Enrollment) =
            MyEnrollmentResponse(
                id = enrollment.id,
                courseId = enrollment.courseId,
                status = enrollment.status,
                enrollmentType = enrollment.enrollmentType,
                tuitionAmountWon = enrollment.tuitionAmountWon,
            )
    }
}
