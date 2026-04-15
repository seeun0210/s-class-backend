package com.sclass.domain.domains.lessonReport.domain

import com.sclass.domain.common.model.BaseTimeEntity
import com.sclass.domain.domains.lessonReport.exception.LessonReportInvalidStatusTransitionException
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime

@Entity
@Table(
    name = "lesson_reports",
    indexes = [
        Index(name = "idx_lesson_reports_lesson", columnList = "lesson_id"),
        Index(name = "idx_lesson_reports_status", columnList = "status"),
    ],
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_lesson_reports_lesson_version",
            columnNames = ["lesson_id", "version"],
        ),
    ],
)
class LessonReport(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    @Column(name = "lesson_id", nullable = false)
    val lessonId: Long,
    @Column(nullable = false)
    val version: Int,
    @Column(name = "submitted_by_user_id", nullable = false, length = 26)
    val submittedByUserId: String,
    @Column(columnDefinition = "TEXT", nullable = false)
    var content: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: LessonReportStatus = LessonReportStatus.PENDING_REVIEW,
    @Column(name = "reviewed_by_user_id", length = 26)
    var reviewedByUserId: String? = null,
    @Column(name = "reviewed_at")
    var reviewedAt: LocalDateTime? = null,
    @Column(name = "reject_reason", length = 1000)
    var rejectReason: String? = null,
) : BaseTimeEntity() {
    fun approve(
        reviewerUserId: String,
        at: LocalDateTime = LocalDateTime.now(),
    ) {
        validateTransition(LessonReportStatus.APPROVED)
        this.status = LessonReportStatus.APPROVED
        this.reviewedByUserId = reviewerUserId
        this.reviewedAt = at
    }

    fun reject(
        reviewerUserId: String,
        reason: String,
        at: LocalDateTime = LocalDateTime.now(),
    ) {
        validateTransition(LessonReportStatus.REJECTED)
        this.status = LessonReportStatus.REJECTED
        this.reviewedByUserId = reviewerUserId
        this.reviewedAt = at
        this.rejectReason = reason
    }

    private fun validateTransition(target: LessonReportStatus) {
        val allowed =
            when (target) {
                LessonReportStatus.PENDING_REVIEW -> emptySet()
                LessonReportStatus.APPROVED -> setOf(LessonReportStatus.PENDING_REVIEW)
                LessonReportStatus.REJECTED -> setOf(LessonReportStatus.PENDING_REVIEW)
            }
        if (status !in allowed) throw LessonReportInvalidStatusTransitionException()
    }
}
