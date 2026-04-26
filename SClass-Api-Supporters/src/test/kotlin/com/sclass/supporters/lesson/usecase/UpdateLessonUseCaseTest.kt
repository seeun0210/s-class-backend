package com.sclass.supporters.lesson.usecase

import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.domain.domains.lesson.domain.Lesson
import com.sclass.domain.domains.lesson.domain.LessonGoogleMeet
import com.sclass.domain.domains.lesson.domain.LessonStatus
import com.sclass.domain.domains.lesson.domain.LessonType
import com.sclass.domain.domains.lesson.exception.LessonScheduleSyncRequiredException
import com.sclass.domain.domains.lesson.exception.LessonUnauthorizedAccessException
import com.sclass.supporters.lesson.dto.UpdateLessonRequest
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class UpdateLessonUseCaseTest {
    private lateinit var lessonAdaptor: LessonAdaptor
    private lateinit var useCase: UpdateLessonUseCase

    private val studentUserId = "student-user-id-0000000001"
    private val teacherUserId = "teacher-user-id-0000000001"

    @BeforeEach
    fun setUp() {
        lessonAdaptor = mockk()
        useCase = UpdateLessonUseCase(lessonAdaptor)
    }

    @Test
    fun `deprecated PATCH는 수업 이름만 수정한다`() {
        val lesson = lesson(name = "수학 1회차")
        every { lessonAdaptor.findById(1L) } returns lesson

        val response =
            useCase.execute(
                userId = teacherUserId,
                lessonId = 1L,
                request = UpdateLessonRequest(name = "수학 보강"),
            )

        assertAll(
            { assertEquals("수학 보강", lesson.name) },
            { assertEquals("수학 보강", response.name) },
        )
    }

    @Test
    fun `deprecated PATCH는 scheduledAt을 직접 수정할 수 없다`() {
        val originalScheduledAt = LocalDateTime.of(2026, 5, 1, 20, 0)
        val lesson = lesson(scheduledAt = originalScheduledAt)
        every { lessonAdaptor.findById(1L) } returns lesson

        assertThrows<LessonScheduleSyncRequiredException> {
            useCase.execute(
                userId = teacherUserId,
                lessonId = 1L,
                request = UpdateLessonRequest(scheduledAt = LocalDateTime.of(2026, 5, 2, 21, 0)),
            )
        }

        assertEquals(originalScheduledAt, lesson.scheduledAt)
    }

    @Test
    fun `Google Meet이 연결된 수업은 deprecated PATCH로 이름을 직접 수정할 수 없다`() {
        val lesson =
            lesson(
                name = "수학 1회차",
                googleMeet = LessonGoogleMeet("event-id", "https://meet.google.com/abc-defg-hij", "abc-defg-hij"),
            )
        every { lessonAdaptor.findById(1L) } returns lesson

        assertThrows<LessonScheduleSyncRequiredException> {
            useCase.execute(
                userId = teacherUserId,
                lessonId = 1L,
                request = UpdateLessonRequest(name = "수학 보강"),
            )
        }

        assertEquals("수학 1회차", lesson.name)
    }

    @Test
    fun `담당 선생님이 아니면 deprecated PATCH 수정 불가`() {
        every { lessonAdaptor.findById(1L) } returns lesson()

        assertThrows<LessonUnauthorizedAccessException> {
            useCase.execute(
                userId = "other-teacher-id-000000001",
                lessonId = 1L,
                request = UpdateLessonRequest(name = "변경 불가"),
            )
        }
    }

    private fun lesson(
        name: String = "수학 1회차",
        scheduledAt: LocalDateTime? = null,
        googleMeet: LessonGoogleMeet? = null,
    ) = Lesson(
        id = 1L,
        lessonType = LessonType.COURSE,
        enrollmentId = 1L,
        studentUserId = studentUserId,
        assignedTeacherUserId = teacherUserId,
        lessonNumber = 1,
        name = name,
        scheduledAt = scheduledAt,
        status = LessonStatus.SCHEDULED,
        googleMeet = googleMeet,
    )
}
