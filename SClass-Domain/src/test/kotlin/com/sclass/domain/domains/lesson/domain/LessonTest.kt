package com.sclass.domain.domains.lesson.domain

import com.sclass.domain.domains.lesson.exception.LessonInvalidStatusTransitionException
import com.sclass.domain.domains.lesson.exception.LessonSubstituteAssignNotAllowedException
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class LessonTest {
    private val student = "student-user-id-0000000001"
    private val assignedTeacher = "assigned-teacher-id-0000001"
    private val substitute = "substitute-teacher-id-00001"

    private fun newLesson(
        status: LessonStatus = LessonStatus.SCHEDULED,
        substituteTeacherUserId: String? = null,
    ) = Lesson(
        lessonType = LessonType.COURSE,
        studentUserId = student,
        assignedTeacherUserId = assignedTeacher,
        substituteTeacherUserId = substituteTeacherUserId,
        name = "lesson",
        status = status,
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
        val now = LocalDateTime.now()
        lesson.complete(assignedTeacher, now)
        assertAll(
            { assertEquals(LessonStatus.COMPLETED, lesson.status) },
            { assertEquals(assignedTeacher, lesson.actualTeacherUserId) },
            { assertEquals(now, lesson.completedAt) },
        )
    }

    @Test
    fun `COMPLETED에서 다시 complete 호출 시 예외`() {
        val lesson = newLesson(status = LessonStatus.COMPLETED)
        assertThrows<LessonInvalidStatusTransitionException> {
            lesson.complete(assignedTeacher)
        }
    }

    @Test
    fun `CANCELLED 상태에서 대타 배정 시 예외`() {
        val lesson = newLesson(status = LessonStatus.CANCELLED)
        assertThrows<LessonSubstituteAssignNotAllowedException> {
            lesson.assignSubstitute(substitute)
        }
    }
}
