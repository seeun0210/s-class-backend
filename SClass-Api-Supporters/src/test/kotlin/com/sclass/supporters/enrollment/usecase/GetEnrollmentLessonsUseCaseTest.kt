package com.sclass.supporters.enrollment.usecase

import com.sclass.domain.domains.course.adaptor.CourseAdaptor
import com.sclass.domain.domains.course.domain.Course
import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.enrollment.domain.Enrollment
import com.sclass.domain.domains.enrollment.exception.EnrollmentUnauthorizedAccessException
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
import org.junit.jupiter.api.assertThrows

class GetEnrollmentLessonsUseCaseTest {
    private lateinit var enrollmentAdaptor: EnrollmentAdaptor
    private lateinit var courseAdaptor: CourseAdaptor
    private lateinit var lessonAdaptor: LessonAdaptor
    private lateinit var useCase: GetEnrollmentLessonsUseCase

    private val courseId = 1L
    private val enrollmentId = 1L
    private val teacherUserId = "teacher-user-id-00000000001"
    private val studentUserId = "student-user-id-00000000001"

    @BeforeEach
    fun setUp() {
        enrollmentAdaptor = mockk()
        courseAdaptor = mockk()
        lessonAdaptor = mockk()
        useCase = GetEnrollmentLessonsUseCase(enrollmentAdaptor, courseAdaptor, lessonAdaptor)
    }

    private fun enrollment() =
        mockk<Enrollment>(relaxed = true) {
            every { studentUserId } returns this@GetEnrollmentLessonsUseCaseTest.studentUserId
            every { courseId } returns this@GetEnrollmentLessonsUseCaseTest.courseId
        }

    private fun course() =
        Course(
            id = courseId,
            productId = "product-id-0000000000000001",
            teacherUserId = teacherUserId,
            name = "수학반",
            status = CourseStatus.ACTIVE,
        )

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
    fun `학생이 본인 수강의 레슨 목록을 조회한다`() {
        val lessons = listOf(lesson(1), lesson(2))
        every { enrollmentAdaptor.findById(enrollmentId) } returns enrollment()
        every { courseAdaptor.findById(courseId) } returns course()
        every { lessonAdaptor.findAllByEnrollment(enrollmentId) } returns lessons

        val result = useCase.execute(studentUserId, enrollmentId)

        assertAll(
            { assertEquals(2, result.size) },
            { assertEquals(LessonType.COURSE, result.first().lessonType) },
            { assertEquals(enrollmentId, result.first().enrollmentId) },
            { assertEquals(LessonStatus.SCHEDULED, result.first().status) },
        )
    }

    @Test
    fun `선생님이 담당 코스의 레슨 목록을 조회한다`() {
        val lessons = listOf(lesson(1), lesson(2))
        every { enrollmentAdaptor.findById(enrollmentId) } returns enrollment()
        every { courseAdaptor.findById(courseId) } returns course()
        every { lessonAdaptor.findAllByEnrollment(enrollmentId) } returns lessons

        val result = useCase.execute(teacherUserId, enrollmentId)

        assertAll(
            { assertEquals(2, result.size) },
            { assertEquals(LessonType.COURSE, result.first().lessonType) },
        )
    }

    @Test
    fun `레슨이 없으면 빈 목록을 반환한다`() {
        every { enrollmentAdaptor.findById(enrollmentId) } returns enrollment()
        every { courseAdaptor.findById(courseId) } returns course()
        every { lessonAdaptor.findAllByEnrollment(enrollmentId) } returns emptyList()

        val result = useCase.execute(studentUserId, enrollmentId)

        assertEquals(0, result.size)
    }

    @Test
    fun `학생도 선생님도 아니면 EnrollmentUnauthorizedAccessException이 발생한다`() {
        every { enrollmentAdaptor.findById(enrollmentId) } returns enrollment()
        every { courseAdaptor.findById(courseId) } returns course()

        assertThrows<EnrollmentUnauthorizedAccessException> {
            useCase.execute("other-user-id-0000000000001", enrollmentId)
        }
    }
}
