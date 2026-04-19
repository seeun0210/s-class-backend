package com.sclass.supporters.catalog.usecase

import com.sclass.domain.domains.course.adaptor.CourseAdaptor
import com.sclass.domain.domains.course.domain.Course
import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.domain.domains.course.dto.CatalogCourseDto
import com.sclass.domain.domains.product.domain.CourseProduct
import com.sclass.domain.domains.teacher.domain.MajorCategory
import com.sclass.domain.domains.teacher.domain.Teacher
import com.sclass.domain.domains.teacher.domain.TeacherEducation
import com.sclass.domain.domains.teacher.domain.TeacherProfile
import com.sclass.domain.domains.user.domain.AuthProvider
import com.sclass.domain.domains.user.domain.User
import com.sclass.infrastructure.s3.ThumbnailUrlResolver
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

class GetCatalogCourseListUseCaseTest {
    private lateinit var courseAdaptor: CourseAdaptor
    private lateinit var thumbnailUrlResolver: ThumbnailUrlResolver
    private lateinit var useCase: GetCatalogCourseListUseCase

    @BeforeEach
    fun setUp() {
        courseAdaptor = mockk()
        thumbnailUrlResolver = mockk()
        every { thumbnailUrlResolver.resolve(any()) } answers {
            firstArg<String?>()?.let { "https://static.test.sclass.click/course_thumbnail/$it" }
        }
        useCase = GetCatalogCourseListUseCase(courseAdaptor, thumbnailUrlResolver)
    }

    private fun makeDto(
        courseId: Long = 1L,
        productId: String = "product-id-0000000000000001",
        courseName: String = "수학 기초",
        description: String? = "수학 심화 과정",
        priceWon: Int = 300000,
        totalLessons: Int = 12,
        maxEnrollments: Int = 10,
        liveEnrollmentCount: Long = 0,
        thumbnailFileId: String? = null,
        teacherName: String = "김선생",
        selfIntroduction: String? = "안녕하세요",
        majorCategory: MajorCategory? = MajorCategory.ENGINEERING,
        university: String? = "서울대학교",
        major: String? = "컴퓨터공학",
    ): CatalogCourseDto {
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
        return CatalogCourseDto(
            course =
                Course(
                    id = courseId,
                    productId = productId,
                    teacherUserId = user.id,
                    status = CourseStatus.LISTED,
                    maxEnrollments = maxEnrollments,
                ),
            courseProduct =
                CourseProduct(
                    name = courseName,
                    priceWon = priceWon,
                    totalLessons = totalLessons,
                    description = description,
                    thumbnailFileId = thumbnailFileId,
                ),
            teacher = teacher,
            teacherUser = user,
            liveEnrollmentCount = liveEnrollmentCount,
        )
    }

    @Test
    fun `카탈로그 코스 페이지를 반환한다`() {
        val dto = makeDto()
        val pageable = PageRequest.of(0, 20)
        every { courseAdaptor.findAllCatalogCourses(pageable) } returns PageImpl(listOf(dto), pageable, 1)

        val result = useCase.execute(pageable)

        assertAll(
            { assertEquals(1L, result.totalElements) },
            { assertEquals(1, result.totalPages) },
            { assertEquals(0, result.currentPage) },
            { assertEquals(1L, result.content[0].id) },
            { assertEquals("product-id-0000000000000001", result.content[0].productId) },
            { assertEquals("수학 기초", result.content[0].name) },
            { assertEquals("수학 심화 과정", result.content[0].description) },
            { assertEquals(300000, result.content[0].priceWon) },
            { assertEquals(12, result.content[0].totalLessons) },
            { assertEquals("김선생", result.content[0].teacher.name) },
            { assertEquals("안녕하세요", result.content[0].teacher.selfIntroduction) },
            { assertEquals(MajorCategory.ENGINEERING, result.content[0].teacher.majorCategory) },
            { assertEquals("서울대학교", result.content[0].teacher.university) },
            { assertEquals("컴퓨터공학", result.content[0].teacher.major) },
        )
    }

    @Test
    fun `카탈로그에 공개된 코스가 없으면 빈 페이지를 반환한다`() {
        val pageable = PageRequest.of(0, 20)
        every { courseAdaptor.findAllCatalogCourses(pageable) } returns PageImpl(emptyList(), pageable, 0)

        val result = useCase.execute(pageable)

        assertAll(
            { assertTrue(result.content.isEmpty()) },
            { assertEquals(0L, result.totalElements) },
        )
    }

    @Test
    fun `remainingSeats는 maxEnrollments에서 liveEnrollmentCount를 뺀 값`() {
        val dto = makeDto(maxEnrollments = 10, liveEnrollmentCount = 3)
        val pageable = PageRequest.of(0, 20)
        every { courseAdaptor.findAllCatalogCourses(pageable) } returns PageImpl(listOf(dto), pageable, 1)

        val result = useCase.execute(pageable)

        assertAll(
            { assertEquals(10, result.content[0].maxEnrollments) },
            { assertEquals(7L, result.content[0].remainingSeats) },
        )
    }

    @Test
    fun `liveEnrollmentCount가 maxEnrollments를 초과해도 remainingSeats는 0 이상`() {
        val dto = makeDto(maxEnrollments = 10, liveEnrollmentCount = 12)
        val pageable = PageRequest.of(0, 20)
        every { courseAdaptor.findAllCatalogCourses(pageable) } returns PageImpl(listOf(dto), pageable, 1)

        val result = useCase.execute(pageable)

        assertEquals(0L, result.content[0].remainingSeats)
    }

    @Test
    fun `썸네일이 있으면 thumbnailUrl을 CDN 경로로 채운다`() {
        val dto = makeDto(thumbnailFileId = "file-id-001")
        val pageable = PageRequest.of(0, 20)
        every { courseAdaptor.findAllCatalogCourses(pageable) } returns PageImpl(listOf(dto), pageable, 1)

        val result = useCase.execute(pageable)

        assertEquals(
            "https://static.test.sclass.click/course_thumbnail/file-id-001",
            result.content[0].thumbnailUrl,
        )
    }

    @Test
    fun `썸네일이 없으면 thumbnailUrl은 null`() {
        val dto = makeDto(thumbnailFileId = null)
        val pageable = PageRequest.of(0, 20)
        every { courseAdaptor.findAllCatalogCourses(pageable) } returns PageImpl(listOf(dto), pageable, 1)

        val result = useCase.execute(pageable)

        assertEquals(null, result.content[0].thumbnailUrl)
    }

    @Test
    fun `여러 코스를 조회 순서대로 반환한다`() {
        val dtos =
            listOf(
                makeDto(courseId = 1L, courseName = "수학 기초"),
                makeDto(courseId = 2L, courseName = "영어 회화", teacherName = "이선생"),
            )
        val pageable = PageRequest.of(0, 20)
        every { courseAdaptor.findAllCatalogCourses(pageable) } returns PageImpl(dtos, pageable, 2)

        val result = useCase.execute(pageable)

        assertAll(
            { assertEquals(2, result.content.size) },
            { assertEquals("수학 기초", result.content[0].name) },
            { assertEquals("영어 회화", result.content[1].name) },
            { assertEquals("이선생", result.content[1].teacher.name) },
        )
    }
}
