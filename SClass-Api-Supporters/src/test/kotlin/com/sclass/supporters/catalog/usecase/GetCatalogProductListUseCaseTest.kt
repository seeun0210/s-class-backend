package com.sclass.supporters.catalog.usecase

import com.sclass.domain.domains.coin.adaptor.CoinPackageAdaptor
import com.sclass.domain.domains.coin.domain.CoinPackage
import com.sclass.domain.domains.course.adaptor.CourseAdaptor
import com.sclass.domain.domains.course.domain.Course
import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.domain.domains.course.dto.CatalogCourseDto
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CourseProduct
import com.sclass.domain.domains.product.domain.Product
import com.sclass.domain.domains.product.domain.ProductCatalogSort
import com.sclass.domain.domains.product.domain.ProductType
import com.sclass.domain.domains.product.domain.RollingMembershipProduct
import com.sclass.domain.domains.teacher.domain.MajorCategory
import com.sclass.domain.domains.teacher.domain.Teacher
import com.sclass.domain.domains.teacher.domain.TeacherEducation
import com.sclass.domain.domains.teacher.domain.TeacherProfile
import com.sclass.domain.domains.user.domain.AuthProvider
import com.sclass.domain.domains.user.domain.User
import com.sclass.infrastructure.s3.ThumbnailUrlResolver
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

class GetCatalogProductListUseCaseTest {
    private lateinit var productAdaptor: ProductAdaptor
    private lateinit var courseAdaptor: CourseAdaptor
    private lateinit var enrollmentAdaptor: EnrollmentAdaptor
    private lateinit var coinPackageAdaptor: CoinPackageAdaptor
    private lateinit var thumbnailUrlResolver: ThumbnailUrlResolver
    private lateinit var useCase: GetCatalogProductListUseCase

    @BeforeEach
    fun setUp() {
        productAdaptor = mockk()
        courseAdaptor = mockk()
        enrollmentAdaptor = mockk()
        coinPackageAdaptor = mockk()
        thumbnailUrlResolver = mockk()
        every { thumbnailUrlResolver.resolve(any()) } answers {
            firstArg<String?>()?.let { "https://cdn.test/$it" }
        }

        useCase =
            GetCatalogProductListUseCase(
                productAdaptor = productAdaptor,
                courseAdaptor = courseAdaptor,
                enrollmentAdaptor = enrollmentAdaptor,
                coinPackageAdaptor = coinPackageAdaptor,
                thumbnailUrlResolver = thumbnailUrlResolver,
            )
    }

    @Test
    fun `course와 membership 상품을 함께 products 응답으로 반환한다`() {
        val pageable = PageRequest.of(0, 20)
        val courseProduct =
            CourseProduct(
                name = "수학 코스",
                priceWon = 300000,
                description = "기초 수학",
                thumbnailFileId = "course-thumb",
                totalLessons = 12,
                curriculum = "커리큘럼",
                requiresMatching = false,
            )
        val membershipProduct =
            RollingMembershipProduct(
                name = "롤링 멤버십",
                priceWon = 99000,
                description = "매월 제공",
                thumbnailFileId = "membership-thumb",
                maxEnrollments = 10,
                coinPackageId = "coin-package-id",
                periodDays = 30,
            )
        val page = PageImpl(listOf<Product>(courseProduct, membershipProduct), pageable, 2)

        every {
            productAdaptor.findVisibleCatalogProducts(
                types = listOf(ProductType.COURSE, ProductType.ROLLING_MEMBERSHIP),
                sort = ProductCatalogSort.POPULARITY,
                pageable = pageable,
            )
        } returns page
        every { courseAdaptor.findAllCatalogCoursesByProductIds(listOf(courseProduct.id)) } returns
            mapOf(courseProduct.id to listOf(makeCatalogCourseDto(courseProduct)))
        every { enrollmentAdaptor.countLiveMembershipEnrollmentsByProductIds(listOf(membershipProduct.id)) } returns
            mapOf(membershipProduct.id to 3L)
        every { coinPackageAdaptor.findAllByIds(setOf("coin-package-id")) } returns
            mapOf(
                "coin-package-id" to
                    CoinPackage(
                        id = "coin-package-id",
                        name = "코인 패키지",
                        priceWon = 99000,
                        coinAmount = 100,
                    ),
            )

        val result =
            useCase.execute(
                types = listOf(ProductType.COURSE, ProductType.ROLLING_MEMBERSHIP),
                sort = ProductCatalogSort.POPULARITY,
                pageable = pageable,
            )

        assertThat(result.totalElements).isEqualTo(2)
        assertThat(result.content).hasSize(2)

        val course = result.content[0]
        assertThat(course.productType).isEqualTo(ProductType.COURSE)
        assertThat(course.course).isNotNull
        assertThat(course.course?.requiresMatching).isFalse()
        assertThat(course.course?.courseOptions).hasSize(1)
        assertThat(
            course.course
                ?.courseOptions
                ?.first()
                ?.teacher
                ?.name,
        ).isEqualTo("김선생")

        val membership = result.content[1]
        assertThat(membership.productType).isEqualTo(ProductType.ROLLING_MEMBERSHIP)
        assertThat(membership.membership?.periodDays).isEqualTo(30)
        assertThat(membership.membership?.remainingSeats).isEqualTo(7L)
        assertThat(membership.membership?.coinAmount).isEqualTo(100)
    }

    @Test
    fun `매칭형 course product는 course option이 없어도 products 응답으로 반환한다`() {
        val pageable = PageRequest.of(0, 20)
        val matchingCourseProduct =
            CourseProduct(
                name = "매칭형 수학 코스",
                priceWon = 250000,
                totalLessons = 8,
                requiresMatching = true,
            )
        val page = PageImpl(listOf<Product>(matchingCourseProduct), pageable, 1)

        every {
            productAdaptor.findVisibleCatalogProducts(
                types = listOf(ProductType.COURSE),
                sort = ProductCatalogSort.LATEST,
                pageable = pageable,
            )
        } returns page
        every { courseAdaptor.findAllCatalogCoursesByProductIds(listOf(matchingCourseProduct.id)) } returns emptyMap()
        every { enrollmentAdaptor.countLiveMembershipEnrollmentsByProductIds(any()) } returns emptyMap()
        every { coinPackageAdaptor.findAllByIds(any()) } returns emptyMap()

        val result =
            useCase.execute(
                types = listOf(ProductType.COURSE),
                sort = ProductCatalogSort.LATEST,
                pageable = pageable,
            )

        assertThat(result.totalElements).isEqualTo(1)
        assertThat(result.content).hasSize(1)
        assertThat(result.content.first().productId).isEqualTo(matchingCourseProduct.id)
        assertThat(
            result.content
                .first()
                .course
                ?.requiresMatching,
        ).isTrue()
        assertThat(
            result.content
                .first()
                .course
                ?.courseOptions,
        ).isEmpty()
    }

    private fun makeCatalogCourseDto(courseProduct: CourseProduct): CatalogCourseDto {
        val teacherUser =
            User(
                email = "teacher@test.com",
                name = "김선생",
                authProvider = AuthProvider.EMAIL,
            )
        val teacher =
            Teacher(
                user = teacherUser,
                profile = TeacherProfile(selfIntroduction = "안녕하세요"),
                education =
                    TeacherEducation(
                        majorCategory = MajorCategory.ENGINEERING,
                        university = "서울대학교",
                        major = "컴퓨터공학",
                    ),
            )

        return CatalogCourseDto(
            course =
                Course(
                    id = 1L,
                    productId = courseProduct.id,
                    teacherUserId = teacherUser.id,
                    status = CourseStatus.LISTED,
                    maxEnrollments = 10,
                ),
            courseProduct = courseProduct,
            teacher = teacher,
            teacherUser = teacherUser,
            liveEnrollmentCount = 3L,
        )
    }
}
