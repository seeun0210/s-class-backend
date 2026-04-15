package com.sclass.domain.domains.lesson.service

import com.sclass.domain.domains.course.domain.Course
import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.domain.domains.enrollment.domain.Enrollment
import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.domain.domains.lesson.domain.Lesson
import com.sclass.domain.domains.lesson.domain.LessonStatus
import com.sclass.domain.domains.lesson.domain.LessonType
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LessonDomainServiceTest {
    private val lessonAdaptor = mockk<LessonAdaptor>()
    private val service = LessonDomainService(lessonAdaptor)

    private fun enrollment() =
        Enrollment.createByGrant(
            courseId = 1L,
            studentUserId = "student-id-00000000001",
            grantedByUserId = "admin-id-000000000001",
            grantReason = "테스트",
            tuitionAmountWon = 300000,
        )

    private fun course() =
        Course(
            id = 1L,
            productId = "product-id-00000000001",
            teacherUserId = "teacher-id-00000000001",
            name = "수학 기초",
            status = CourseStatus.ACTIVE,
        )

    @Test
    fun `totalLessons만큼 레슨이 생성된다`() {
        val lessonsSlot = slot<List<Lesson>>()
        every { lessonAdaptor.saveAll(capture(lessonsSlot)) } answers { lessonsSlot.captured }

        val result = service.createLessonsForEnrollment(enrollment(), course(), totalLessons = 4)

        assertEquals(4, result.size)
    }

    @Test
    fun `생성된 레슨의 필드가 올바르게 매핑된다`() {
        val lessonsSlot = slot<List<Lesson>>()
        every { lessonAdaptor.saveAll(capture(lessonsSlot)) } answers { lessonsSlot.captured }

        val result = service.createLessonsForEnrollment(enrollment(), course(), totalLessons = 3)

        val first = result[0]
        val last = result[2]
        assertAll(
            { assertEquals(LessonType.COURSE, first.lessonType) },
            { assertEquals("student-id-00000000001", first.studentUserId) },
            { assertEquals("teacher-id-00000000001", first.assignedTeacherUserId) },
            { assertEquals(1, first.lessonNumber) },
            { assertEquals("수학 기초 1회차", first.name) },
            { assertEquals(LessonStatus.SCHEDULED, first.status) },
            { assertEquals(3, last.lessonNumber) },
            { assertEquals("수학 기초 3회차", last.name) },
        )
    }

    @Test
    fun `totalLessons가 0이면 빈 리스트를 반환한다`() {
        every { lessonAdaptor.saveAll(emptyList()) } returns emptyList()

        val result = service.createLessonsForEnrollment(enrollment(), course(), totalLessons = 0)

        assertEquals(0, result.size)
    }
}
