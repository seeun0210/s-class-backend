package com.sclass.supporters.catalog.usecase

import com.sclass.domain.domains.course.adaptor.CourseAdaptor
import com.sclass.domain.domains.course.domain.Course
import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.domain.domains.course.dto.CourseWithTeacherDto
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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetCatalogCourseListUseCaseTest {
    private lateinit var courseAdaptor: CourseAdaptor
    private lateinit var useCase: GetCatalogCourseListUseCase

    @BeforeEach
    fun setUp() {
        courseAdaptor = mockk()
        useCase = GetCatalogCourseListUseCase(courseAdaptor)
    }

    private fun makeDto(
        courseId: Long = 1L,
        productId: String = "product-id-0000000000000001",
        courseName: String = "수학 기초",
        description: String? = "수학 심화 과정",
        priceWon: Int = 300000,
        totalLessons: Int = 12,
        teacherName: String = "김선생",
        selfIntroduction: String? = "안녕하세요",
        majorCategory: MajorCategory? = MajorCategory.ENGINEERING,
        university: String? = "서울대학교",
        major: String? = "컴퓨터공학",
    ): CourseWithTeacherDto {
        val user =
            User(
                email = "teacher@test.com",
                name = teacherName,
                authProvider = AuthProvider.EMAIL,
            )
        val teacher =
            Teacher(
                user = user,
                profile = TeacherProfile(selfIntroduction = selfIntroduction),
                education =
                    TeacherEducation(
                        majorCategory = majorCategory,
                        university = university,
                        major = major,
                    ),
            )
        return CourseWithTeacherDto(
            course =
                Course(
                    id = courseId,
                    productId = productId,
                    teacherUserId = user.id,
                    status = CourseStatus.LISTED,
                ),
            courseProduct =
                CourseProduct(
                    name = courseName,
                    priceWon = priceWon,
                    totalLessons = totalLessons,
                    description = description,
                ),
            teacher = teacher,
            teacherUser = user,
        )
    }

    @Test
    fun `카탈로그 코스 목록을 반환한다`() {
        val dto = makeDto()
        every { courseAdaptor.findAllCatalogCourses() } returns listOf(dto)

        val result = useCase.execute()

        assertAll(
            { assertEquals(1, result.size) },
            { assertEquals(1L, result[0].id) },
            { assertEquals("product-id-0000000000000001", result[0].productId) },
            { assertEquals("수학 기초", result[0].name) },
            { assertEquals("수학 심화 과정", result[0].description) },
            { assertEquals(300000, result[0].priceWon) },
            { assertEquals(12, result[0].totalLessons) },
            { assertEquals("김선생", result[0].teacher.name) },
            { assertEquals("안녕하세요", result[0].teacher.selfIntroduction) },
            { assertEquals(MajorCategory.ENGINEERING, result[0].teacher.majorCategory) },
            { assertEquals("서울대학교", result[0].teacher.university) },
            { assertEquals("컴퓨터공학", result[0].teacher.major) },
        )
    }

    @Test
    fun `카탈로그에 공개된 코스가 없으면 빈 리스트를 반환한다`() {
        every { courseAdaptor.findAllCatalogCourses() } returns emptyList()

        val result = useCase.execute()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `여러 코스를 조회 순서대로 반환한다`() {
        val dtos =
            listOf(
                makeDto(courseId = 1L, courseName = "수학 기초"),
                makeDto(courseId = 2L, courseName = "영어 회화", teacherName = "이선생"),
            )
        every { courseAdaptor.findAllCatalogCourses() } returns dtos

        val result = useCase.execute()

        assertAll(
            { assertEquals(2, result.size) },
            { assertEquals("수학 기초", result[0].name) },
            { assertEquals("영어 회화", result[1].name) },
            { assertEquals("이선생", result[1].teacher.name) },
        )
    }
}
