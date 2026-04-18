package com.sclass.backoffice.course.usecase

import com.sclass.domain.domains.course.adaptor.CourseAdaptor
import com.sclass.domain.domains.course.domain.Course
import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.domain.domains.course.dto.CourseWithTeacherAndEnrollmentCountDto
import com.sclass.domain.domains.product.domain.CourseProduct
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

class GetCourseListUseCaseTest {
    private val courseAdaptor = mockk<CourseAdaptor>()
    private val useCase = GetCourseListUseCase(courseAdaptor)

    private fun createCourseDto(
        teacherUserId: String = "teacher01",
        teacherName: String = "김선생",
        courseName: String = "수학 기초",
        status: CourseStatus = CourseStatus.ACTIVE,
        enrollmentCount: Long = 5,
        totalLessons: Int = 12,
        priceWon: Int = 300000,
    ) = CourseWithTeacherAndEnrollmentCountDto(
        course =
            Course(
                id = 1L,
                productId = "prod01",
                teacherUserId = teacherUserId,
                status = status,
            ),
        courseProduct =
            CourseProduct(
                name = courseName,
                priceWon = priceWon,
                totalLessons = totalLessons,
                description = "설명",
            ),
        teacherName = teacherName,
        enrollmentCount = enrollmentCount,
    )

    @Test
    fun `코스 목록을 페이지로 반환한다`() {
        val dtos =
            listOf(
                createCourseDto(courseName = "수학 기초", teacherName = "김선생"),
                createCourseDto(courseName = "영어 회화", teacherName = "이선생"),
            )
        val pageable = PageRequest.of(0, 20)
        every { courseAdaptor.searchCourses(null, null, pageable) } returns PageImpl(dtos, pageable, 2)

        val result = useCase.execute(null, null, pageable)

        assertAll(
            { assertEquals(2, result.totalElements) },
            { assertEquals(1, result.totalPages) },
            { assertEquals(0, result.currentPage) },
            { assertEquals("수학 기초", result.content[0].name) },
            { assertEquals("김선생", result.content[0].teacherName) },
            { assertEquals("영어 회화", result.content[1].name) },
            { assertEquals("이선생", result.content[1].teacherName) },
        )
    }

    @Test
    fun `teacherUserId 필터를 적용하면 해당 선생님의 코스만 반환한다`() {
        val dto = createCourseDto(teacherUserId = "teacher01", teacherName = "김선생")
        val pageable = PageRequest.of(0, 20)
        every { courseAdaptor.searchCourses("teacher01", null, pageable) } returns PageImpl(listOf(dto), pageable, 1)

        val result = useCase.execute("teacher01", null, pageable)

        assertAll(
            { assertEquals(1, result.totalElements) },
            { assertEquals("teacher01", result.content[0].teacherUserId) },
            { assertEquals("김선생", result.content[0].teacherName) },
        )
    }

    @Test
    fun `status 필터를 적용하면 해당 상태의 코스만 반환한다`() {
        val dto = createCourseDto(status = CourseStatus.DRAFT)
        val pageable = PageRequest.of(0, 20)
        every { courseAdaptor.searchCourses(null, CourseStatus.DRAFT, pageable) } returns PageImpl(listOf(dto), pageable, 1)

        val result = useCase.execute(null, CourseStatus.DRAFT, pageable)

        assertAll(
            { assertEquals(1, result.totalElements) },
            { assertEquals(CourseStatus.DRAFT, result.content[0].status) },
        )
    }

    @Test
    fun `enrollmentCount가 응답에 포함된다`() {
        val dto = createCourseDto(enrollmentCount = 10)
        val pageable = PageRequest.of(0, 20)
        every { courseAdaptor.searchCourses(null, null, pageable) } returns PageImpl(listOf(dto), pageable, 1)

        val result = useCase.execute(null, null, pageable)

        assertEquals(10, result.content[0].enrollmentCount)
    }

    @Test
    fun `totalLessons가 응답에 포함된다`() {
        val dto = createCourseDto(totalLessons = 8)
        val pageable = PageRequest.of(0, 20)
        every { courseAdaptor.searchCourses(null, null, pageable) } returns PageImpl(listOf(dto), pageable, 1)

        val result = useCase.execute(null, null, pageable)

        assertEquals(8, result.content[0].totalLessons)
    }

    @Test
    fun `priceWon이 응답에 포함된다`() {
        val dto = createCourseDto(priceWon = 450000)
        val pageable = PageRequest.of(0, 20)
        every { courseAdaptor.searchCourses(null, null, pageable) } returns PageImpl(listOf(dto), pageable, 1)

        val result = useCase.execute(null, null, pageable)

        assertEquals(450000, result.content[0].priceWon)
    }

    @Test
    fun `코스가 없으면 빈 페이지를 반환한다`() {
        val pageable = PageRequest.of(0, 20)
        every { courseAdaptor.searchCourses(null, null, pageable) } returns PageImpl(emptyList(), pageable, 0)

        val result = useCase.execute(null, null, pageable)

        assertAll(
            { assertEquals(0, result.totalElements) },
            { assertEquals(0, result.content.size) },
        )
    }
}
