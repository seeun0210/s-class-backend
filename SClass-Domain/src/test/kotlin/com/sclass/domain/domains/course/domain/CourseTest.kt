package com.sclass.domain.domains.course.domain

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class CourseTest {
    private fun newListedCourse(
        maxEnrollments: Int = 1,
        enrollmentStartAt: LocalDateTime? = null,
        enrollmentDeadLine: LocalDateTime? = null,
    ) = Course(
        productId = "PRD",
        teacherUserId = "TCH",
        status = CourseStatus.LISTED,
        maxEnrollments = maxEnrollments,
        enrollmentStartAt = enrollmentStartAt,
        enrollmentDeadLine = enrollmentDeadLine,
    )

    @Test
    fun `LISTED 상태에 정원이 남고 기간 제약이 없으면 등록 가능하다`() {
        assertTrue(newListedCourse().canEnroll(LocalDateTime.now(), currentCount = 0))
    }

    @Test
    fun `DRAFT 상태면 등록 불가능하다`() {
        val course =
            Course(
                productId = "PRD",
                teacherUserId = "TCH",
                status = CourseStatus.DRAFT,
            )

        assertFalse(course.canEnroll(LocalDateTime.now(), currentCount = 0))
    }

    @Test
    fun `UNLISTED 상태면 등록 불가능하다`() {
        val course = newListedCourse().also { it.unlist() }

        assertFalse(course.canEnroll(LocalDateTime.now(), currentCount = 0))
    }

    @Test
    fun `ARCHIVED 상태면 등록 불가능하다`() {
        val course = newListedCourse().also { it.archive() }

        assertFalse(course.canEnroll(LocalDateTime.now(), currentCount = 0))
    }

    @Test
    fun `enrollmentStartAt 이전이면 등록 불가능하다`() {
        val now = LocalDateTime.of(2026, 4, 1, 0, 0)
        val course = newListedCourse(enrollmentStartAt = now.plusDays(1))

        assertFalse(course.canEnroll(now, currentCount = 0))
    }

    @Test
    fun `enrollmentStartAt 이후면 등록 가능하다`() {
        val now = LocalDateTime.of(2026, 4, 1, 0, 0)
        val course = newListedCourse(enrollmentStartAt = now.minusDays(1))

        assertTrue(course.canEnroll(now, currentCount = 0))
    }

    @Test
    fun `enrollmentDeadLine 이후면 등록 불가능하다`() {
        val now = LocalDateTime.of(2026, 4, 1, 0, 0)
        val course = newListedCourse(enrollmentDeadLine = now.minusDays(1))

        assertFalse(course.canEnroll(now, currentCount = 0))
    }

    @Test
    fun `enrollmentDeadLine 이전이면 등록 가능하다`() {
        val now = LocalDateTime.of(2026, 4, 1, 0, 0)
        val course = newListedCourse(enrollmentDeadLine = now.plusDays(1))

        assertTrue(course.canEnroll(now, currentCount = 0))
    }

    @Test
    fun `정원이 가득 차면 등록 불가능하다`() {
        val course = newListedCourse(maxEnrollments = 3)

        assertFalse(course.canEnroll(LocalDateTime.now(), currentCount = 3))
    }

    @Test
    fun `정원이 남아있으면 등록 가능하다`() {
        val course = newListedCourse(maxEnrollments = 3)

        assertTrue(course.canEnroll(LocalDateTime.now(), currentCount = 2))
    }
}
