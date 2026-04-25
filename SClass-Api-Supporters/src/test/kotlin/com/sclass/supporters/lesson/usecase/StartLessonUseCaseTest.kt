package com.sclass.supporters.lesson.usecase

import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.domain.domains.lesson.domain.Lesson
import com.sclass.domain.domains.lesson.domain.LessonStatus
import com.sclass.domain.domains.lesson.domain.LessonType
import com.sclass.domain.domains.lesson.exception.LessonAlreadyStartedException
import com.sclass.domain.domains.lesson.exception.LessonInvalidStatusTransitionException
import com.sclass.domain.domains.lesson.exception.LessonInvalidTimeException
import com.sclass.domain.domains.lesson.exception.LessonUnauthorizedAccessException
import com.sclass.supporters.lesson.dto.StartLessonRequest
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

class StartLessonUseCaseTest {
    private lateinit var lessonAdaptor: LessonAdaptor
    private lateinit var useCase: StartLessonUseCase

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
        useCase = StartLessonUseCase(lessonAdaptor, clock)
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
    fun `요청 시각이 없으면 서버 now로 start되고 IN_PROGRESS 전이`() {
        val lesson = newLesson()
        every { lessonAdaptor.findById(1L) } returns lesson

        val response = useCase.execute(assignedTeacher, 1L, StartLessonRequest())

        assertAll(
            { assertEquals(LessonStatus.IN_PROGRESS, lesson.status) },
            { assertEquals(assignedTeacher, lesson.actualTeacherUserId) },
            { assertEquals(fixedNow, lesson.startedAt) },
            { assertEquals(LessonStatus.IN_PROGRESS, response.status) },
        )
    }

    @Test
    fun `요청 시각이 있으면 그 값으로 startedAt이 채워진다`() {
        val lesson = newLesson()
        val customStart = fixedNow.minusMinutes(15)
        every { lessonAdaptor.findById(1L) } returns lesson

        useCase.execute(assignedTeacher, 1L, StartLessonRequest(startedAt = customStart))

        assertEquals(customStart, lesson.startedAt)
    }

    @Test
    fun `start는 scheduledAt을 변경하지 않는다`() {
        val scheduled = fixedNow.minusHours(1)
        val lesson = newLesson(scheduledAt = scheduled)
        every { lessonAdaptor.findById(1L) } returns lesson

        useCase.execute(assignedTeacher, 1L, StartLessonRequest())

        assertEquals(scheduled, lesson.scheduledAt)
    }

    @Test
    fun `담당이 아닌 유저는 start 불가`() {
        every { lessonAdaptor.findById(1L) } returns newLesson()

        assertThrows<LessonUnauthorizedAccessException> {
            useCase.execute("other-user-id", 1L, StartLessonRequest())
        }
    }

    @Test
    fun `이미 COMPLETED인 lesson은 start 불가`() {
        every { lessonAdaptor.findById(1L) } returns newLesson(status = LessonStatus.COMPLETED)

        assertThrows<LessonInvalidStatusTransitionException> {
            useCase.execute(assignedTeacher, 1L, StartLessonRequest())
        }
    }

    @Test
    fun `미래 시각으로 start 요청 시 예외 - now+1초도 거절 (boundary)`() {
        every { lessonAdaptor.findById(1L) } returns newLesson()

        assertThrows<LessonInvalidTimeException> {
            useCase.execute(
                assignedTeacher,
                1L,
                StartLessonRequest(startedAt = fixedNow.plusSeconds(1)),
            )
        }
    }

    @Test
    fun `startedAt이 이미 기록된 lesson은 start 불가`() {
        every { lessonAdaptor.findById(1L) } returns newLesson(startedAt = fixedNow.minusMinutes(10))

        assertThrows<LessonAlreadyStartedException> {
            useCase.execute(assignedTeacher, 1L, StartLessonRequest())
        }
    }
}
