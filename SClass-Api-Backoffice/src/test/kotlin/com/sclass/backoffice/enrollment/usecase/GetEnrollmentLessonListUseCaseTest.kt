package com.sclass.backoffice.enrollment.usecase

import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.domain.domains.lesson.domain.Lesson
import com.sclass.domain.domains.lesson.domain.LessonStatus
import com.sclass.domain.domains.lesson.domain.LessonType
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetEnrollmentLessonListUseCaseTest {
    private val lessonAdaptor = mockk<LessonAdaptor>()
    private val useCase = GetEnrollmentLessonListUseCase(lessonAdaptor)

    private fun createLesson(
        id: Long = 1L,
        lessonNumber: Int = 1,
        name: String = "수학 기초 ${lessonNumber}회차",
        status: LessonStatus = LessonStatus.SCHEDULED,
    ) = Lesson(
        id = id,
        lessonType = LessonType.COURSE,
        enrollmentId = 100L,
        studentUserId = "student-id-00000000001",
        assignedTeacherUserId = "teacher-id-00000000001",
        lessonNumber = lessonNumber,
        name = name,
        status = status,
    )

    @Test
    fun `enrollment의 레슨 목록을 반환한다`() {
        val lessons =
            listOf(
                createLesson(id = 1L, lessonNumber = 1),
                createLesson(id = 2L, lessonNumber = 2),
                createLesson(id = 3L, lessonNumber = 3),
            )
        every { lessonAdaptor.findAllByEnrollment(100L) } returns lessons

        val result = useCase.execute(100L)

        assertAll(
            { assertEquals(3, result.lessons.size) },
            { assertEquals(1, result.lessons[0].lessonNumber) },
            { assertEquals("수학 기초 1회차", result.lessons[0].name) },
            { assertEquals(LessonStatus.SCHEDULED, result.lessons[0].status) },
            { assertEquals(3, result.lessons[2].lessonNumber) },
        )
    }

    @Test
    fun `레슨이 없으면 빈 리스트를 반환한다`() {
        every { lessonAdaptor.findAllByEnrollment(999L) } returns emptyList()

        val result = useCase.execute(999L)

        assertEquals(0, result.lessons.size)
    }
}
