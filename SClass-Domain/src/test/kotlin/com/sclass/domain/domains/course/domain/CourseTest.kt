package com.sclass.domain.domains.course.domain

import com.sclass.domain.domains.course.exception.CourseAlreadyStartedException
import com.sclass.domain.domains.course.exception.CourseInvalidScheduleException
import com.sclass.domain.domains.course.exception.CourseMaxEnrollmentsTooLowException
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class CourseTest {
    private fun newListedCourse(
        maxEnrollments: Int = 1,
        enrollmentStartAt: LocalDateTime? = null,
        enrollmentDeadLine: LocalDateTime? = null,
    ) = Course(
        productId = "PRD",
        teacherUserId = "TCH",
        status = CourseStatus.LISTED,
        maxEnrollments = maxEnrollments,
        enrollmentStartAt = enrollmentStartAt,
        enrollmentDeadLine = enrollmentDeadLine,
    )

    @Test
    fun `LISTED 상태에 정원이 남고 기간 제약이 없으면 등록 가능하다`() {
        assertTrue(newListedCourse().canEnroll(LocalDateTime.now(), currentCount = 0L))
    }

    @Test
    fun `DRAFT 상태면 등록 불가능하다`() {
        val course =
            Course(
                productId = "PRD",
                teacherUserId = "TCH",
                status = CourseStatus.DRAFT,
            )

        assertFalse(course.canEnroll(LocalDateTime.now(), currentCount = 0L))
    }

    @Test
    fun `UNLISTED 상태면 등록 불가능하다`() {
        val course = newListedCourse().also { it.unlist() }

        assertFalse(course.canEnroll(LocalDateTime.now(), currentCount = 0L))
    }

    @Test
    fun `ARCHIVED 상태면 등록 불가능하다`() {
        val course = newListedCourse().also { it.archive() }

        assertFalse(course.canEnroll(LocalDateTime.now(), currentCount = 0L))
    }

    @Test
    fun `enrollmentStartAt 이전이면 등록 불가능하다`() {
        val now = LocalDateTime.of(2026, 4, 1, 0, 0)
        val course = newListedCourse(enrollmentStartAt = now.plusDays(1))

        assertFalse(course.canEnroll(now, currentCount = 0L))
    }

    @Test
    fun `enrollmentStartAt 이후면 등록 가능하다`() {
        val now = LocalDateTime.of(2026, 4, 1, 0, 0)
        val course = newListedCourse(enrollmentStartAt = now.minusDays(1))

        assertTrue(course.canEnroll(now, currentCount = 0L))
    }

    @Test
    fun `enrollmentDeadLine 이후면 등록 불가능하다`() {
        val now = LocalDateTime.of(2026, 4, 1, 0, 0)
        val course = newListedCourse(enrollmentDeadLine = now.minusDays(1))

        assertFalse(course.canEnroll(now, currentCount = 0L))
    }

    @Test
    fun `enrollmentDeadLine 이전이면 등록 가능하다`() {
        val now = LocalDateTime.of(2026, 4, 1, 0, 0)
        val course = newListedCourse(enrollmentDeadLine = now.plusDays(1))

        assertTrue(course.canEnroll(now, currentCount = 0L))
    }

    @Test
    fun `정원이 가득 차면 등록 불가능하다`() {
        val course = newListedCourse(maxEnrollments = 3)

        assertFalse(course.canEnroll(LocalDateTime.now(), currentCount = 3L))
    }

    @Test
    fun `정원이 남아있으면 등록 가능하다`() {
        val course = newListedCourse(maxEnrollments = 3)

        assertTrue(course.canEnroll(LocalDateTime.now(), currentCount = 2L))
    }

    @Test
    fun `startAt 이전이면 hasStarted는 false`() {
        val now = LocalDateTime.of(2026, 4, 1, 0, 0)
        val course = newListedCourse().also { it.startAt = now.plusDays(1) }

        assertFalse(course.hasStarted(now))
    }

    @Test
    fun `startAt과 같거나 이후면 hasStarted는 true`() {
        val now = LocalDateTime.of(2026, 4, 1, 0, 0)
        val course = newListedCourse().also { it.startAt = now }

        assertTrue(course.hasStarted(now))
    }

    @Test
    fun `startAt이 null이면 hasStarted는 false`() {
        val course = newListedCourse()

        assertFalse(course.hasStarted(LocalDateTime.now()))
    }

    @Test
    fun `이미 시작된 코스는 모집 조건을 변경할 수 없다`() {
        val now = LocalDateTime.of(2026, 4, 1, 0, 0)
        val course = newListedCourse().also { it.startAt = now.minusDays(1) }

        assertThrows(CourseAlreadyStartedException::class.java) {
            course.updateEnrollmentConstraints(now, newMaxEnrollments = 10, null, null, currentLiveCount = 0)
        }
    }

    @Test
    fun `maxEnrollments를 현재 등록자 수보다 낮추면 예외`() {
        val course = newListedCourse(maxEnrollments = 10)

        assertThrows(CourseMaxEnrollmentsTooLowException::class.java) {
            course.updateEnrollmentConstraints(
                now = LocalDateTime.of(2026, 4, 1, 0, 0),
                newMaxEnrollments = 2,
                newEnrollmentStartAt = null,
                newEnrollmentDeadLine = null,
                currentLiveCount = 3,
            )
        }
    }

    @Test
    fun `모집 시작이 모집 마감보다 뒤면 예외`() {
        val now = LocalDateTime.of(2026, 4, 1, 0, 0)
        val course = newListedCourse()

        assertThrows(CourseInvalidScheduleException::class.java) {
            course.updateEnrollmentConstraints(
                now = now,
                newMaxEnrollments = null,
                newEnrollmentStartAt = now.plusDays(10),
                newEnrollmentDeadLine = now.plusDays(5),
                currentLiveCount = 0,
            )
        }
    }

    @Test
    fun `정상 입력이면 모집 조건이 반영된다`() {
        val now = LocalDateTime.of(2026, 4, 1, 0, 0)
        val course = newListedCourse()

        course.updateEnrollmentConstraints(
            now = now,
            newMaxEnrollments = 20,
            newEnrollmentStartAt = now.plusDays(1),
            newEnrollmentDeadLine = now.plusDays(5),
            currentLiveCount = 0,
        )

        assertTrue(course.maxEnrollments == 20)
        assertTrue(course.enrollmentStartAt == now.plusDays(1))
        assertTrue(course.enrollmentDeadLine == now.plusDays(5))
    }

    @Test
    fun `이미 시작된 코스는 일정을 변경할 수 없다`() {
        val now = LocalDateTime.of(2026, 4, 1, 0, 0)
        val course = newListedCourse().also { it.startAt = now.minusDays(1) }

        assertThrows(CourseAlreadyStartedException::class.java) {
            course.updateSchedule(now, newStartTime = now.plusDays(1), newEndAt = now.plusDays(5))
        }
    }

    @Test
    fun `startAt이 endAt보다 뒤면 예외`() {
        val now = LocalDateTime.of(2026, 4, 1, 0, 0)
        val course = newListedCourse()

        assertThrows(CourseInvalidScheduleException::class.java) {
            course.updateSchedule(now, newStartTime = now.plusDays(10), newEndAt = now.plusDays(5))
        }
    }

    @Test
    fun `모집 마감이 개강보다 뒤면 예외`() {
        val now = LocalDateTime.of(2026, 4, 1, 0, 0)
        val course =
            newListedCourse(
                enrollmentStartAt = now.plusDays(1),
                enrollmentDeadLine = now.plusDays(10),
            )

        assertThrows(CourseInvalidScheduleException::class.java) {
            course.updateSchedule(now, newStartTime = now.plusDays(5), newEndAt = now.plusDays(30))
        }
    }

    @Test
    fun `정상 입력이면 일정이 반영된다`() {
        val now = LocalDateTime.of(2026, 4, 1, 0, 0)
        val course = newListedCourse()

        course.updateSchedule(now, newStartTime = now.plusDays(10), newEndAt = now.plusDays(40))

        assertTrue(course.startAt == now.plusDays(10))
        assertTrue(course.endAt == now.plusDays(40))
    }
}
