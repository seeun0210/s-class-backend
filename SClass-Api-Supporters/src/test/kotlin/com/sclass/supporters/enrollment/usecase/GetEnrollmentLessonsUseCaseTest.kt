package com.sclass.supporters.enrollment.usecase

import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.domain.domains.lesson.domain.Lesson
import com.sclass.domain.domains.lesson.domain.LessonStatus
import com.sclass.domain.domains.lesson.domain.LessonType
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetEnrollmentLessonsUseCaseTest {
    private lateinit var lessonAdaptor: LessonAdaptor
    private lateinit var useCase: GetEnrollmentLessonsUseCase

    private val enrollmentId = 1L
    private val teacherUserId = "teacher-user-id-00000000001"
    private val studentUserId = "student-user-id-00000000001"

    @BeforeEach
    fun setUp() {
        lessonAdaptor = mockk()
        useCase = GetEnrollmentLessonsUseCase(lessonAdaptor)
    }

    private fun lesson(lessonNumber: Int = 1) =
        Lesson(
            lessonType = LessonType.COURSE,
            enrollmentId = enrollmentId,
            studentUserId = studentUserId,
            assignedTeacherUserId = teacherUserId,
            lessonNumber = lessonNumber,
            name = "수학 ${lessonNumber}회차",
            teacherPayoutAmountWon = 50_000,
        )

    @Test
    fun `수강신청에 속한 레슨 목록을 반환한다`() {
        val lessons = listOf(lesson(1), lesson(2))
        every { lessonAdaptor.findAllByEnrollment(enrollmentId) } returns lessons

        val result = useCase.execute(enrollmentId)

        assertAll(
            { assertEquals(2, result.size) },
            { assertEquals(LessonType.COURSE, result.first().lessonType) },
            { assertEquals(enrollmentId, result.first().enrollmentId) },
            { assertEquals(LessonStatus.SCHEDULED, result.first().status) },
        )
    }

    @Test
    fun `레슨이 없으면 빈 목록을 반환한다`() {
        every { lessonAdaptor.findAllByEnrollment(enrollmentId) } returns emptyList()

        val result = useCase.execute(enrollmentId)

        assertEquals(0, result.size)
    }
}
