package com.sclass.backoffice.enrollment.dto

import com.sclass.domain.domains.enrollment.domain.EnrollmentStatus
import com.sclass.domain.domains.enrollment.domain.EnrollmentType
import com.sclass.domain.domains.enrollment.dto.EnrollmentWithDetailDto
import java.time.LocalDateTime

data class EnrollmentPageResponse(
    val content: List<EnrollmentListResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
)

data class EnrollmentListResponse(
    val id: Long,
    val courseId: Long,
    val courseName: String,
    val studentUserId: String,
    val studentName: String,
    val teacherName: String,
    val enrollmentType: EnrollmentType,
    val tuitionAmountWon: Int,
    val teacherPayoutPerLessonWon: Int,
    val status: EnrollmentStatus,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(dto: EnrollmentWithDetailDto) =
            EnrollmentListResponse(
                id = dto.enrollment.id,
                courseId = dto.enrollment.courseId,
                courseName = dto.courseName,
                studentUserId = dto.enrollment.studentUserId,
                studentName = dto.studentName,
                teacherName = dto.teacherName,
                enrollmentType = dto.enrollment.enrollmentType,
                tuitionAmountWon = dto.enrollment.tuitionAmountWon,
                teacherPayoutPerLessonWon = dto.enrollment.teacherPayoutPerLessonWon,
                status = dto.enrollment.status,
                createdAt = dto.enrollment.createdAt,
            )
    }
}
