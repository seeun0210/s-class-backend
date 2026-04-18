package com.sclass.supporters.catalog.usecase

import com.sclass.domain.domains.course.adaptor.CourseAdaptor
import com.sclass.domain.domains.course.domain.Course
import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.domain.domains.course.dto.CourseWithTeacherDto
import com.sclass.domain.domains.course.exception.CourseNotFoundException
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.product.domain.CourseProduct
import com.sclass.domain.domains.teacher.domain.MajorCategory
import com.sclass.domain.domains.teacher.domain.Teacher
import com.sclass.domain.domains.teacher.domain.TeacherEducation
import com.sclass.domain.domains.teacher.domain.TeacherProfile
import com.sclass.domain.domains.user.domain.AuthProvider
import com.sclass.domain.domains.user.domain.User
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class GetCatalogCourseDetailUseCaseTest {
    private lateinit var courseAdaptor: CourseAdaptor
    private lateinit var enrollmentAdaptor: EnrollmentAdaptor
    private lateinit var useCase: GetCatalogCourseDetailUseCase

    @BeforeEach
    fun setUp() {
        courseAdaptor = mockk()
        enrollmentAdaptor = mockk()
        useCase = GetCatalogCourseDetailUseCase(courseAdaptor, enrollmentAdaptor)
    }

    private fun makeDto(
        courseId: Long = 1L,
        maxEnrollments: Int = 10,
        enrollmentStartAt: LocalDateTime? = LocalDateTime.of(2026, 4, 1, 0, 0),
        enrollmentDeadLine: LocalDateTime? = LocalDateTime.of(2026, 4, 30, 0, 0),
        startAt: LocalDateTime? = LocalDateTime.of(2026, 5, 1, 0, 0),
        endAt: LocalDateTime? = LocalDateTime.of(2026, 6, 30, 0, 0),
        curriculum: String? = "주 1회 12주 커리큘럼",
        thumbnailFileId: String? = "file-id-00000000000000001",
    ): CourseWithTeacherDto {
        val user =
            User(
                email = "teacher@test.com",
                name = "김선생",
                authProvider = AuthProvider.EMAIL,
            )
        val teacher =
            Teacher(
                user = user,
                profile = TeacherProfile(selfIntroduction = "안녕하세요"),
                education =
                    TeacherEducation(
                        majorCategory = MajorCategory.ENGINEERING,
                        university = "서울대학교",
                        major = "컴퓨터공학",
                    ),
            )
        return CourseWithTeacherDto(
            course =
                Course(
                    id = courseId,
                    productId = "product-id-0000000000000001",
                    teacherUserId = user.id,
                    status = CourseStatus.LISTED,
                    maxEnrollments = maxEnrollments,
                    enrollmentStartAt = enrollmentStartAt,
                    enrollmentDeadLine = enrollmentDeadLine,
                    startAt = startAt,
                    endAt = endAt,
                ),
            courseProduct =
                CourseProduct(
                    name = "수학 기초",
                    priceWon = 300000,
                    totalLessons = 12,
                    description = "수학 심화 과정",
                    thumbnailFileId = thumbnailFileId,
                    curriculum = curriculum,
                ),
            teacher = teacher,
            teacherUser = user,
        )
    }

    @Test
    fun `카탈로그 코스 상세와 남은 좌석을 반환한다`() {
        val dto = makeDto(maxEnrollments = 10)
        every { courseAdaptor.findCatalogCourseById(1L) } returns dto
        every { enrollmentAdaptor.countLiveEnrollments(1L) } returns 3L

        val result = useCase.execute(1L)

        assertAll(
            { assertEquals(1L, result.id) },
            { assertEquals("수학 기초", result.name) },
            { assertEquals("수학 심화 과정", result.description) },
            { assertEquals("주 1회 12주 커리큘럼", result.curriculum) },
            { assertEquals("file-id-00000000000000001", result.thumbnailFileId) },
            { assertEquals(300000, result.priceWon) },
            { assertEquals(12, result.totalLessons) },
            { assertEquals(10, result.maxEnrollments) },
            { assertEquals(7L, result.remainingSeats) },
            { assertEquals(LocalDateTime.of(2026, 4, 1, 0, 0), result.enrollmentStartAt) },
            { assertEquals(LocalDateTime.of(2026, 4, 30, 0, 0), result.enrollmentDeadLine) },
            { assertEquals(LocalDateTime.of(2026, 5, 1, 0, 0), result.startAt) },
            { assertEquals(LocalDateTime.of(2026, 6, 30, 0, 0), result.endAt) },
            { assertEquals("김선생", result.teacher.name) },
            { assertEquals(MajorCategory.ENGINEERING, result.teacher.majorCategory) },
        )
    }

    @Test
    fun `활성 등록 수가 정원을 넘으면 남은 좌석은 0이다`() {
        val dto = makeDto(maxEnrollments = 5)
        every { courseAdaptor.findCatalogCourseById(1L) } returns dto
        every { enrollmentAdaptor.countLiveEnrollments(1L) } returns 10L

        val result = useCase.execute(1L)

        assertEquals(0L, result.remainingSeats)
    }

    @Test
    fun `코스가 카탈로그에 없으면 CourseNotFoundException 을 던진다`() {
        every { courseAdaptor.findCatalogCourseById(999L) } throws CourseNotFoundException()

        assertThrows(CourseNotFoundException::class.java) {
            useCase.execute(999L)
        }
    }
}
