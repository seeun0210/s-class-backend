package com.sclass.domain.domains.lesson.domain

import com.sclass.domain.domains.lesson.exception.LessonAlreadyCompletedException
import com.sclass.domain.domains.lesson.exception.LessonAlreadyStartedException
import com.sclass.domain.domains.lesson.exception.LessonInvalidStatusTransitionException
import com.sclass.domain.domains.lesson.exception.LessonInvalidTimeException
import com.sclass.domain.domains.lesson.exception.LessonSubstituteAssignNotAllowedException
import com.sclass.domain.domains.lesson.exception.LessonSubstituteSameAsAssignedException
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId

class LessonTest {
    private val student = "student-user-id-0000000001"
    private val assignedTeacher = "assigned-teacher-id-0000001"
    private val substitute = "substitute-teacher-id-00001"

    private val fixedNow = LocalDateTime.of(2026, 4, 26, 14, 0)
    private val clock =
        Clock.fixed(
            fixedNow.atZone(ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault(),
        )

    private fun newLesson(
        status: LessonStatus = LessonStatus.SCHEDULED,
        substituteTeacherUserId: String? = null,
        scheduledAt: LocalDateTime? = null,
        startedAt: LocalDateTime? = null,
        completedAt: LocalDateTime? = null,
    ) = Lesson(
        lessonType = LessonType.COURSE,
        studentUserId = student,
        assignedTeacherUserId = assignedTeacher,
        substituteTeacherUserId = substituteTeacherUserId,
        name = "lesson",
        status = status,
        scheduledAt = scheduledAt,
        startedAt = startedAt,
        completedAt = completedAt,
    )

    @Test
    fun `substitute가 없으면 effectiveTeacher는 assignedTeacher`() {
        val lesson = newLesson()
        assertAll(
            { assertEquals(assignedTeacher, lesson.effectiveTeacherUserId) },
            { assertTrue(lesson.isTeacher(assignedTeacher)) },
            { assertTrue(!lesson.isTeacher(substitute)) },
        )
    }

    @Test
    fun `substitute가 배정되면 effectiveTeacher는 substitute`() {
        val lesson = newLesson(substituteTeacherUserId = substitute)
        assertAll(
            { assertEquals(substitute, lesson.effectiveTeacherUserId) },
            { assertTrue(lesson.isTeacher(substitute)) },
            { assertTrue(!lesson.isTeacher(assignedTeacher)) },
        )
    }

    @Test
    fun `SCHEDULED 상태에서만 대타 배정 가능`() {
        val lesson = newLesson()
        lesson.assignSubstitute(substitute)
        assertEquals(substitute, lesson.substituteTeacherUserId)
    }

    @Test
    fun `SCHEDULED가 아니면 대타 배정 시 예외`() {
        val lesson = newLesson(status = LessonStatus.IN_PROGRESS)
        assertThrows<LessonSubstituteAssignNotAllowedException> {
            lesson.assignSubstitute(substitute)
        }
    }

    @Test
    fun `대타 해제 시 substituteTeacherUserId가 null`() {
        val lesson = newLesson(substituteTeacherUserId = substitute)
        lesson.unassignSubstitute()
        assertNull(lesson.substituteTeacherUserId)
    }

    @Test
    fun `SCHEDULED에서 바로 COMPLETED 전이 가능`() {
        val lesson = newLesson()

        lesson.complete(assignedTeacher, fixedNow, clock)

        assertAll(
            { assertEquals(LessonStatus.COMPLETED, lesson.status) },
            { assertEquals(assignedTeacher, lesson.actualTeacherUserId) },
            { assertEquals(fixedNow, lesson.completedAt) },
        )
    }

    @Test
    fun `COMPLETED에서 다시 complete 호출 시 예외`() {
        val lesson = newLesson(status = LessonStatus.COMPLETED)
        assertThrows<LessonInvalidStatusTransitionException> {
            lesson.complete(assignedTeacher, fixedNow, clock)
        }
    }

    @Test
    fun `assignedTeacher를 대타로 배정 시 예외`() {
        val lesson = newLesson()
        assertThrows<LessonSubstituteSameAsAssignedException> {
            lesson.assignSubstitute(assignedTeacher)
        }
    }

    @Test
    fun `CANCELLED 상태에서 대타 배정 시 예외`() {
        val lesson = newLesson(status = LessonStatus.CANCELLED)
        assertThrows<LessonSubstituteAssignNotAllowedException> {
            lesson.assignSubstitute(substitute)
        }
    }

    @Test
    fun `start 호출 시 IN_PROGRESS로 전이되고 startedAt이 정확히 기록된다`() {
        val lesson = newLesson()
        val startTime = fixedNow.minusMinutes(5)

        lesson.start(assignedTeacher, startTime, clock)

        assertAll(
            { assertEquals(LessonStatus.IN_PROGRESS, lesson.status) },
            { assertEquals(assignedTeacher, lesson.actualTeacherUserId) },
            { assertEquals(startTime, lesson.startedAt) },
        )
    }

    @Test
    fun `at이 null이면 clock 기반 now로 startedAt이 채워진다`() {
        val lesson = newLesson()

        lesson.start(assignedTeacher, at = null, clock = clock)

        assertEquals(fixedNow, lesson.startedAt)
    }

    @Test
    fun `start는 scheduledAt에 영향을 주지 않는다`() {
        val scheduled = fixedNow.minusHours(1)
        val lesson = newLesson(scheduledAt = scheduled)

        lesson.start(assignedTeacher, fixedNow, clock)

        assertEquals(scheduled, lesson.scheduledAt)
    }

    @Test
    fun `미래 시각으로 start 호출 시 예외 - now+1초도 거절 (boundary)`() {
        val lesson = newLesson()
        assertThrows<LessonInvalidTimeException> {
            lesson.start(assignedTeacher, fixedNow.plusSeconds(1), clock)
        }
    }

    @Test
    fun `complete는 scheduledAt에 영향을 주지 않는다`() {
        val scheduled = fixedNow.minusHours(2)
        val lesson = newLesson(scheduledAt = scheduled)

        lesson.complete(assignedTeacher, fixedNow, clock)

        assertEquals(scheduled, lesson.scheduledAt)
    }

    @Test
    fun `미래 시각으로 complete 호출 시 예외 - now+1초도 거절 (boundary)`() {
        val lesson = newLesson()
        assertThrows<LessonInvalidTimeException> {
            lesson.complete(assignedTeacher, fixedNow.plusSeconds(1), clock)
        }
    }

    @Test
    fun `completedAt이 startedAt보다 이전이면 예외`() {
        val started = fixedNow.minusMinutes(10)
        val lesson = newLesson(status = LessonStatus.IN_PROGRESS, startedAt = started)

        assertThrows<LessonInvalidTimeException> {
            lesson.complete(assignedTeacher, started.minusSeconds(1), clock)
        }
    }

    @Test
    fun `IN_PROGRESS에서 complete 호출 시 COMPLETED로 전이되고 completedAt이 정확히 기록된다`() {
        val started = fixedNow.minusMinutes(30)
        val lesson = newLesson(status = LessonStatus.IN_PROGRESS, startedAt = started)

        lesson.complete(assignedTeacher, fixedNow, clock)

        assertAll(
            { assertEquals(LessonStatus.COMPLETED, lesson.status) },
            { assertEquals(assignedTeacher, lesson.actualTeacherUserId) },
            { assertEquals(fixedNow, lesson.completedAt) },
            { assertEquals(started, lesson.startedAt) },
        )
    }

    @Test
    fun `startedAt이 이미 있으면 start 호출 시 예외`() {
        val lesson = newLesson(startedAt = fixedNow.minusMinutes(10))
        assertThrows<LessonAlreadyStartedException> {
            lesson.start(assignedTeacher, fixedNow, clock)
        }
    }

    @Test
    fun `completedAt이 이미 있으면 complete 호출 시 예외`() {
        val lesson =
            newLesson(
                status = LessonStatus.IN_PROGRESS,
                startedAt = fixedNow.minusMinutes(30),
                completedAt = fixedNow.minusMinutes(5),
            )
        assertThrows<LessonAlreadyCompletedException> {
            lesson.complete(assignedTeacher, fixedNow, clock)
        }
    }

    @Test
    fun `record 호출 시 startedAt과 completedAt이 모두 정확히 기록되고 COMPLETED로 전이`() {
        val lesson = newLesson()
        val started = fixedNow.minusMinutes(60)
        val completed = fixedNow.minusMinutes(10)

        lesson.record(assignedTeacher, started, completed, clock)

        assertAll(
            { assertEquals(LessonStatus.COMPLETED, lesson.status) },
            { assertEquals(assignedTeacher, lesson.actualTeacherUserId) },
            { assertEquals(started, lesson.startedAt) },
            { assertEquals(completed, lesson.completedAt) },
        )
    }

    @Test
    fun `record는 scheduledAt에 영향을 주지 않는다`() {
        val scheduled = fixedNow.minusHours(3)
        val lesson = newLesson(scheduledAt = scheduled)

        lesson.record(
            assignedTeacher,
            fixedNow.minusMinutes(60),
            fixedNow.minusMinutes(10),
            clock,
        )

        assertEquals(scheduled, lesson.scheduledAt)
    }

    @Test
    fun `이미 startedAt이 있으면 record 호출 시 예외`() {
        val lesson =
            newLesson(
                status = LessonStatus.IN_PROGRESS,
                startedAt = fixedNow.minusMinutes(30),
            )
        assertThrows<LessonAlreadyStartedException> {
            lesson.record(
                assignedTeacher,
                fixedNow.minusMinutes(60),
                fixedNow.minusMinutes(10),
                clock,
            )
        }
    }

    @Test
    fun `record의 시작 시각이 미래면 예외 - now+1초도 거절 (boundary)`() {
        val lesson = newLesson()
        assertThrows<LessonInvalidTimeException> {
            lesson.record(
                assignedTeacher,
                fixedNow.plusSeconds(1),
                fixedNow.plusMinutes(60),
                clock,
            )
        }
    }

    @Test
    fun `record의 종료 시각이 시작 시각보다 이전이면 예외`() {
        val lesson = newLesson()
        val started = fixedNow.minusMinutes(10)
        assertThrows<LessonInvalidTimeException> {
            lesson.record(assignedTeacher, started, started.minusSeconds(1), clock)
        }
    }
}
