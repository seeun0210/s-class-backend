package com.sclass.domain.domains.course.domain

import com.sclass.domain.common.model.BaseTimeEntity
import com.sclass.domain.domains.course.exception.CourseAlreadyStartedException
import com.sclass.domain.domains.course.exception.CourseInvalidScheduleException
import com.sclass.domain.domains.course.exception.CourseInvalidStatusTransitionException
import com.sclass.domain.domains.course.exception.CourseMaxEnrollmentsTooLowException
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
    name = "courses",
    indexes = [
        Index(name = "idx_courses_teacher", columnList = "teacher_user_id"),
        Index(name = "idx_courses_product", columnList = "product_id"),
        Index(name = "idx_courses_status", columnList = "status"),
    ],
)
class Course(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "product_id", nullable = false, length = 26)
    val productId: String,

    @Column(name = "teacher_user_id", nullable = false, length = 26)
    val teacherUserId: String,

    @Column(name = "organization_id", length = 26)
    val organizationId: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: CourseStatus = CourseStatus.DRAFT,

    @Column(name = "max_enrollments", nullable = false)
    var maxEnrollments: Int = 1,

    @Column(name = "enrollment_start_at")
    var enrollmentStartAt: LocalDateTime? = null,

    @Column(name = "enrollment_deadline")
    var enrollmentDeadLine: LocalDateTime? = null,

    @Column(name = "start_at")
    var startAt: LocalDateTime? = null,

    @Column(name = "end_at")
    var endAt: LocalDateTime? = null,
) : BaseTimeEntity() {
    fun list() {
        validateTransition(CourseStatus.LISTED)
        this.status = CourseStatus.LISTED
    }

    fun unlist() {
        validateTransition(CourseStatus.UNLISTED)
        this.status = CourseStatus.UNLISTED
    }

    fun archive() {
        validateTransition(CourseStatus.ARCHIVED)
        this.status = CourseStatus.ARCHIVED
    }

    fun canEnroll(
        now: LocalDateTime,
        currentCount: Int,
    ): Boolean {
        if (status !== CourseStatus.LISTED)return false
        if (enrollmentStartAt !== null && now.isBefore(enrollmentStartAt))return false
        if (enrollmentDeadLine !== null && now.isAfter(enrollmentDeadLine))return false
        if (currentCount >= maxEnrollments) return false
        return true
    }

    private fun validateTransition(target: CourseStatus) {
        val allowed =
            when (target) {
                CourseStatus.DRAFT -> emptySet()
                CourseStatus.LISTED -> setOf(CourseStatus.DRAFT, CourseStatus.LISTED, CourseStatus.UNLISTED)
                CourseStatus.UNLISTED -> setOf(CourseStatus.LISTED, CourseStatus.UNLISTED)
                CourseStatus.ARCHIVED ->
                    setOf(CourseStatus.DRAFT, CourseStatus.LISTED, CourseStatus.UNLISTED, CourseStatus.ARCHIVED)
            }
        if (status !in allowed) throw CourseInvalidStatusTransitionException()
    }

    fun hasStarted(now: LocalDateTime): Boolean = startAt !== null && !now.isBefore(startAt)

    fun updateEnrollmentConstraints(
        now: LocalDateTime,
        newMaxEnrollments: Int?,
        newEnrollmentStartAt: LocalDateTime?,
        newEnrollmentDeadLine: LocalDateTime?,
        currentLiveCount: Int,
    ) {
        if (hasStarted(now)) throw CourseAlreadyStartedException()

        val nextMax = newMaxEnrollments ?: maxEnrollments
        val nextStart = newEnrollmentStartAt ?: enrollmentStartAt
        val nextDeadLine = newEnrollmentDeadLine ?: enrollmentDeadLine

        if (nextMax < currentLiveCount)throw CourseMaxEnrollmentsTooLowException()
        validateSchedule(nextStart, nextDeadLine, startAt, endAt)

        maxEnrollments = nextMax
        enrollmentStartAt = nextStart
        enrollmentDeadLine = nextDeadLine
    }

    fun updateSchedule(
        now: LocalDateTime,
        newStartTime: LocalDateTime?,
        newEndAt: LocalDateTime?,
    ) {
        if (hasStarted(now)) throw CourseAlreadyStartedException()

        val nextStart = newStartTime ?: startAt
        val nextEnd = newEndAt ?: endAt
        validateSchedule(enrollmentStartAt, enrollmentDeadLine, nextStart, nextEnd)

        startAt = nextStart
        endAt = nextEnd
    }

    private fun validateSchedule(
        enrollStart: LocalDateTime?,
        enrollDeadline: LocalDateTime?,
        courseStart: LocalDateTime?,
        courseEnd: LocalDateTime?,
    ) {
        if (enrollStart != null && enrollDeadline != null && !enrollStart.isBefore(enrollDeadline)) {
            throw CourseInvalidScheduleException()
        }
        if (enrollDeadline != null && courseStart != null && enrollDeadline.isAfter(courseStart)) {
            throw CourseInvalidScheduleException()
        }
        if (courseStart != null && courseEnd != null && !courseStart.isBefore(courseEnd)) {
            throw CourseInvalidScheduleException()
        }
    }
}
