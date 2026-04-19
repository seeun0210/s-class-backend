package com.sclass.supporters.enrollment.dto

import com.sclass.domain.domains.enrollment.domain.EnrollmentStatus
import com.sclass.domain.domains.enrollment.domain.EnrollmentType
import com.sclass.domain.domains.enrollment.dto.EnrollmentWithStudentDto

data class EnrollmentWithStudentResponse(
    val id: Long,
    val courseId: Long?,
    val status: EnrollmentStatus,
    val enrollmentType: EnrollmentType,
    val tuitionAmountWon: Int,
    val student: StudentSummary,
) {
    data class StudentSummary(
        val userId: String,
        val name: String,
        val email: String,
        val phoneNumber: String?,
    )

    companion object {
        fun from(dto: EnrollmentWithStudentDto) =
            EnrollmentWithStudentResponse(
                id = dto.enrollment.id,
                courseId = dto.enrollment.courseId,
                status = dto.enrollment.status,
                enrollmentType = dto.enrollment.enrollmentType,
                tuitionAmountWon = dto.enrollment.tuitionAmountWon,
                student =
                    StudentSummary(
                        userId = dto.enrollment.studentUserId,
                        name = dto.student?.name ?: "",
                        email = dto.student?.email ?: "",
                        phoneNumber = dto.student?.phoneNumber,
                    ),
            )
    }
}
