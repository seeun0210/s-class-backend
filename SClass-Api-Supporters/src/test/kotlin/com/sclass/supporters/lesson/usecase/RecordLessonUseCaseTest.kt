package com.sclass.supporters.lesson.usecase

import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.domain.domains.lesson.domain.Lesson
import com.sclass.domain.domains.lesson.domain.LessonStatus
import com.sclass.domain.domains.lesson.domain.LessonType
import com.sclass.domain.domains.lesson.exception.LessonAlreadyStartedException
import com.sclass.domain.domains.lesson.exception.LessonInvalidTimeException
import com.sclass.domain.domains.lesson.exception.LessonUnauthorizedAccessException
import com.sclass.supporters.lesson.dto.RecordLessonRequest
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId

class RecordLessonUseCaseTest {
    private lateinit var lessonAdaptor: LessonAdaptor
    private lateinit var useCase: RecordLessonUseCase

    private val student = "student-user-id-0000000001"
    private val assignedTeacher = "assigned-teacher-id-0000001"

    private val fixedNow = LocalDateTime.of(2026, 4, 26, 14, 0)
    private val clock =
        Clock.fixed(
            fixedNow.atZone(ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault(),
        )

    @BeforeEach
    fun setUp() {
        lessonAdaptor = mockk()
        every { lessonAdaptor.save(any()) } answers { firstArg() }
        useCase = RecordLessonUseCase(lessonAdaptor, clock)
    }

    private fun newLesson(
        status: LessonStatus = LessonStatus.SCHEDULED,
        scheduledAt: LocalDateTime? = null,
        startedAt: LocalDateTime? = null,
    ) = Lesson(
        id = 1L,
        lessonType = LessonType.COURSE,
        studentUserId = student,
        assignedTeacherUserId = assignedTeacher,
        name = "lesson",
        status = status,
        scheduledAt = scheduledAt,
        startedAt = startedAt,
    )

    @Test
    fun `record 호출 시 두 시각이 모두 기록되고 COMPLETED로 전이`() {
        val lesson = newLesson()
        val started = fixedNow.minusMinutes(60)
        val completed = fixedNow.minusMinutes(10)
        every { lessonAdaptor.findById(1L) } returns lesson

        val response =
            useCase.execute(
                assignedTeacher,
                1L,
                RecordLessonRequest(startedAt = started, completedAt = completed),
            )

        assertAll(
            { assertEquals(LessonStatus.COMPLETED, lesson.status) },
            { assertEquals(assignedTeacher, lesson.actualTeacherUserId) },
            { assertEquals(started, lesson.startedAt) },
            { assertEquals(completed, lesson.completedAt) },
            { assertEquals(LessonStatus.COMPLETED, response.status) },
        )
    }

    @Test
    fun `record는 scheduledAt을 변경하지 않는다`() {
        val scheduled = fixedNow.minusHours(3)
        val lesson = newLesson(scheduledAt = scheduled)
        every { lessonAdaptor.findById(1L) } returns lesson

        useCase.execute(
            assignedTeacher,
            1L,
            RecordLessonRequest(
                startedAt = fixedNow.minusMinutes(60),
                completedAt = fixedNow.minusMinutes(10),
            ),
        )

        assertEquals(scheduled, lesson.scheduledAt)
    }

    @Test
    fun `담당이 아닌 유저는 record 불가`() {
        every { lessonAdaptor.findById(1L) } returns newLesson()

        assertThrows<LessonUnauthorizedAccessException> {
            useCase.execute(
                "other-user-id",
                1L,
                RecordLessonRequest(
                    startedAt = fixedNow.minusMinutes(60),
                    completedAt = fixedNow.minusMinutes(10),
                ),
            )
        }
    }

    @Test
    fun `이미 startedAt이 기록된 lesson은 record 불가`() {
        every { lessonAdaptor.findById(1L) } returns
            newLesson(
                status = LessonStatus.IN_PROGRESS,
                startedAt = fixedNow.minusMinutes(30),
            )

        assertThrows<LessonAlreadyStartedException> {
            useCase.execute(
                assignedTeacher,
                1L,
                RecordLessonRequest(
                    startedAt = fixedNow.minusMinutes(60),
                    completedAt = fixedNow.minusMinutes(10),
                ),
            )
        }
    }

    @Test
    fun `종료 시각이 시작 시각보다 이전이면 예외`() {
        val started = fixedNow.minusMinutes(10)
        every { lessonAdaptor.findById(1L) } returns newLesson()

        assertThrows<LessonInvalidTimeException> {
            useCase.execute(
                assignedTeacher,
                1L,
                RecordLessonRequest(startedAt = started, completedAt = started.minusSeconds(1)),
            )
        }
    }

    @Test
    fun `시작 시각이 미래면 예외 - now+1초도 거절 (boundary)`() {
        every { lessonAdaptor.findById(1L) } returns newLesson()

        assertThrows<LessonInvalidTimeException> {
            useCase.execute(
                assignedTeacher,
                1L,
                RecordLessonRequest(
                    startedAt = fixedNow.plusSeconds(1),
                    completedAt = fixedNow.plusMinutes(60),
                ),
            )
        }
    }
}
