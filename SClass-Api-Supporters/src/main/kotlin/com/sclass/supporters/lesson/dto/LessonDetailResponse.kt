package com.sclass.supporters.lesson.dto

import com.sclass.domain.domains.lesson.domain.Lesson
import com.sclass.domain.domains.lesson.domain.LessonStatus
import com.sclass.domain.domains.lesson.domain.LessonType
import com.sclass.supporters.inquiry.dto.InquiryPlanResponse
import com.sclass.supporters.student.dto.StudentProfileResponse
import java.time.LocalDateTime

data class LessonDetailResponse(
    val id: Long,
    val name: String,
    val lessonNumber: Int?,
    val lessonType: LessonType,
    val enrollmentId: Long?,
    val sourceCommissionId: Long?,
    val studentUserId: String,
    val assignedTeacherUserId: String,
    val substituteTeacherUserId: String?,
    val student: StudentProfileResponse,
    val status: LessonStatus,
    val scheduledAt: LocalDateTime?,
    val startedAt: LocalDateTime?,
    val completedAt: LocalDateTime?,
    val inquiryPlans: List<InquiryPlanResponse>,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(
            lesson: Lesson,
            student: StudentProfileResponse,
            inquiryPlans: List<InquiryPlanResponse>,
        ) = LessonDetailResponse(
            id = lesson.id,
            name = lesson.name,
            lessonNumber = lesson.lessonNumber,
            lessonType = lesson.lessonType,
            enrollmentId = lesson.enrollmentId,
            sourceCommissionId = lesson.sourceCommissionId,
            studentUserId = lesson.studentUserId,
            assignedTeacherUserId = lesson.assignedTeacherUserId,
            substituteTeacherUserId = lesson.substituteTeacherUserId,
            student = student,
            status = lesson.status,
            scheduledAt = lesson.scheduledAt,
            startedAt = lesson.startedAt,
            completedAt = lesson.completedAt,
            inquiryPlans = inquiryPlans,
            createdAt = lesson.createdAt,
        )
    }
}
