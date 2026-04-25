package com.sclass.supporters.lesson.usecase

import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.domain.domains.lesson.domain.Lesson
import com.sclass.domain.domains.lesson.domain.LessonStatus
import com.sclass.domain.domains.lesson.domain.LessonType
import com.sclass.domain.domains.lesson.exception.LessonInvalidStatusTransitionException
import com.sclass.domain.domains.lesson.exception.LessonInvalidTimeException
import com.sclass.domain.domains.lesson.exception.LessonUnauthorizedAccessException
import com.sclass.supporters.lesson.dto.CompleteLessonRequest
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class CompleteLessonUseCaseTest {
    private lateinit var lessonAdaptor: LessonAdaptor
    private lateinit var useCase: CompleteLessonUseCase

    private val student = "student-user-id-0000000001"
    private val assignedTeacher = "assigned-teacher-id-0000001"

    @BeforeEach
    fun setUp() {
        lessonAdaptor = mockk()
        useCase = CompleteLessonUseCase(lessonAdaptor)
    }

    private fun newLesson(
        status: LessonStatus = LessonStatus.IN_PROGRESS,
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
    fun `요청 시각이 없으면 서버 now로 complete되고 COMPLETED 전이`() {
        val lesson = newLesson(startedAt = LocalDateTime.now().minusMinutes(30))
        every { lessonAdaptor.findById(1L) } returns lesson

        val response = useCase.execute(assignedTeacher, 1L, CompleteLessonRequest())

        assertAll(
            { assertEquals(LessonStatus.COMPLETED, lesson.status) },
            { assertEquals(assignedTeacher, lesson.actualTeacherUserId) },
            { assertNotNull(lesson.completedAt) },
            { assertEquals(LessonStatus.COMPLETED, response.status) },
        )
    }

    @Test
    fun `요청 시각이 있으면 그 값으로 completedAt이 채워진다`() {
        val started = LocalDateTime.now().minusMinutes(60)
        val lesson = newLesson(startedAt = started)
        val customComplete = LocalDateTime.now().minusMinutes(5)
        every { lessonAdaptor.findById(1L) } returns lesson

        useCase.execute(assignedTeacher, 1L, CompleteLessonRequest(completedAt = customComplete))

        assertEquals(customComplete, lesson.completedAt)
    }

    @Test
    fun `complete는 scheduledAt과 startedAt을 변경하지 않는다`() {
        val scheduled = LocalDateTime.now().minusHours(2)
        val started = LocalDateTime.now().minusMinutes(45)
        val lesson = newLesson(scheduledAt = scheduled, startedAt = started)
        every { lessonAdaptor.findById(1L) } returns lesson

        useCase.execute(assignedTeacher, 1L, CompleteLessonRequest())

        assertAll(
            { assertEquals(scheduled, lesson.scheduledAt) },
            { assertEquals(started, lesson.startedAt) },
        )
    }

    @Test
    fun `담당이 아닌 유저는 complete 불가`() {
        every { lessonAdaptor.findById(1L) } returns newLesson()

        assertThrows<LessonUnauthorizedAccessException> {
            useCase.execute("other-user-id", 1L, CompleteLessonRequest())
        }
    }

    @Test
    fun `이미 COMPLETED인 lesson은 다시 complete 불가`() {
        every { lessonAdaptor.findById(1L) } returns newLesson(status = LessonStatus.COMPLETED)

        assertThrows<LessonInvalidStatusTransitionException> {
            useCase.execute(assignedTeacher, 1L, CompleteLessonRequest())
        }
    }

    @Test
    fun `미래 시각으로 complete 요청 시 예외`() {
        every { lessonAdaptor.findById(1L) } returns newLesson()

        assertThrows<LessonInvalidTimeException> {
            useCase.execute(
                assignedTeacher,
                1L,
                CompleteLessonRequest(completedAt = LocalDateTime.now().plusMinutes(10)),
            )
        }
    }

    @Test
    fun `completedAt이 startedAt 이전이면 예외`() {
        val started = LocalDateTime.now().minusMinutes(10)
        every { lessonAdaptor.findById(1L) } returns newLesson(startedAt = started)

        assertThrows<LessonInvalidTimeException> {
            useCase.execute(
                assignedTeacher,
                1L,
                CompleteLessonRequest(completedAt = started.minusMinutes(1)),
            )
        }
    }
}
