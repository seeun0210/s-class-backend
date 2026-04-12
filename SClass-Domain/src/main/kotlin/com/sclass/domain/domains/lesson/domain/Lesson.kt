package com.sclass.domain.domains.lesson.domain

import com.sclass.domain.common.model.BaseTimeEntity
import com.sclass.domain.domains.lesson.exception.LessonInvalidStatusTransitionException
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(
    name = "lessons",
    indexes = [
        Index(name = "idx_lessons_enrollment", columnList = "enrollment_id"),
        Index(name = "idx_lessons_commission", columnList = "source_commission_id"),
        Index(name = "idx_lessons_student", columnList = "student_user_id"),
        Index(name = "idx_lessons_assigned_teacher", columnList = "assigned_teacher_user_id"),
        Index(name = "idx_lessons_actual_teacher", columnList = "actual_teacher_user_id"),
        Index(name = "idx_lessons_status", columnList = "status"),
        Index(name = "idx_lessons_scheduled_at", columnList = "scheduled_at"),
    ],
)
class Lesson(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Enumerated(EnumType.STRING)
    @Column(name = "lesson_type", nullable = false, length = 20)
    val lessonType: LessonType,

    @Column(name = "enrollment_id")
    val enrollmentId: Long? = null,

    @Column(name = "source_commission_id")
    val sourceCommissionId: Long? = null,

    @Column(name = "student_user_id", nullable = false, length = 26)
    val studentUserId: String,

    @Column(name = "assigned_teacher_user_id", nullable = false, length = 26)
    val assignedTeacherUserId: String,

    @Column(name = "actual_teacher_user_id", length = 26)
    var actualTeacherUserId: String? = null,

    @Column(name = "lesson_number")
    val lessonNumber: Int? = null,

    @Column(nullable = false, length = 200)
    var name: String,

    @Column(name = "scheduled_at")
    var scheduledAt: LocalDateTime? = null,

    @Column(name = "started_at")
    var startedAt: LocalDateTime? = null,

    @Column(name = "completed_at")
    var completedAt: LocalDateTime? = null,

    // 정산용 스냅샷 (Enrollment/Commission에서 복사)
    @Column(name = "teacher_payout_amount_won", nullable = false)
    val teacherPayoutAmountWon: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: LessonStatus = LessonStatus.SCHEDULED,
) : BaseTimeEntity() {
    fun start(
        actualTeacherUserId: String,
        at: LocalDateTime = LocalDateTime.now(),
    ) {
        validateTransition(LessonStatus.IN_PROGRESS)
        this.actualTeacherUserId = actualTeacherUserId
        this.startedAt = at
        this.status = LessonStatus.IN_PROGRESS
    }

    fun complete(at: LocalDateTime = LocalDateTime.now()) {
        validateTransition(LessonStatus.COMPLETED)
        this.completedAt = at
        this.status = LessonStatus.COMPLETED
    }

    fun cancel() {
        validateTransition(LessonStatus.CANCELLED)
        this.status = LessonStatus.CANCELLED
    }

    fun updateSchedule(
        name: String?,
        scheduledAt: LocalDateTime?,
    ) {
        if (status != LessonStatus.SCHEDULED) {
            throw LessonInvalidStatusTransitionException()
        }
        name?.let { this.name = it }
        scheduledAt?.let { this.scheduledAt = it }
    }

    private fun validateTransition(target: LessonStatus) {
        val allowed =
            when (target) {
                LessonStatus.SCHEDULED -> emptySet()
                LessonStatus.IN_PROGRESS -> setOf(LessonStatus.SCHEDULED)
                LessonStatus.COMPLETED -> setOf(LessonStatus.IN_PROGRESS)
                LessonStatus.CANCELLED -> setOf(LessonStatus.SCHEDULED, LessonStatus.IN_PROGRESS)
            }
        require(status in allowed) { "Cannot transition from $status to $target" }
    }
}
