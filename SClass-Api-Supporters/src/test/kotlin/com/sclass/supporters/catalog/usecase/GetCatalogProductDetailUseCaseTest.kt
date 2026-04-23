package com.sclass.supporters.catalog.usecase

import com.sclass.domain.domains.coin.adaptor.CoinPackageAdaptor
import com.sclass.domain.domains.coin.domain.CoinPackage
import com.sclass.domain.domains.course.adaptor.CourseAdaptor
import com.sclass.domain.domains.course.domain.Course
import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.domain.domains.course.dto.CatalogCourseDto
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CohortMembershipProduct
import com.sclass.domain.domains.product.domain.CourseProduct
import com.sclass.domain.domains.product.exception.ProductNotFoundException
import com.sclass.domain.domains.teacher.domain.Teacher
import com.sclass.domain.domains.user.domain.AuthProvider
import com.sclass.domain.domains.user.domain.User
import com.sclass.infrastructure.s3.ThumbnailUrlResolver
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class GetCatalogProductDetailUseCaseTest {
    private lateinit var productAdaptor: ProductAdaptor
    private lateinit var courseAdaptor: CourseAdaptor
    private lateinit var enrollmentAdaptor: EnrollmentAdaptor
    private lateinit var coinPackageAdaptor: CoinPackageAdaptor
    private lateinit var thumbnailUrlResolver: ThumbnailUrlResolver
    private lateinit var useCase: GetCatalogProductDetailUseCase

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
            GetCatalogProductDetailUseCase(
                productAdaptor = productAdaptor,
                courseAdaptor = courseAdaptor,
                enrollmentAdaptor = enrollmentAdaptor,
                coinPackageAdaptor = coinPackageAdaptor,
                thumbnailUrlResolver = thumbnailUrlResolver,
            )
    }

    @Test
    fun `course product 상세는 course options를 포함한다`() {
        val product =
            CourseProduct(
                name = "수학 코스",
                priceWon = 300000,
                description = "기초 수학",
                thumbnailFileId = "course-thumb",
                totalLessons = 12,
                curriculum = "커리큘럼",
                requiresMatching = true,
            )
        every { productAdaptor.findVisibleCatalogProductById(product.id) } returns product
        every { courseAdaptor.findAllCatalogCoursesByProductId(product.id) } returns listOf(makeCatalogCourseDto(product))

        val result = useCase.execute(product.id)

        assertThat(result.productId).isEqualTo(product.id)
        assertThat(result.course?.requiresMatching).isTrue()
        assertThat(result.course?.courseOptions).hasSize(1)
        assertThat(result.membership).isNull()
    }

    @Test
    fun `membership product 상세는 membership 정보를 포함한다`() {
        val product =
            CohortMembershipProduct(
                name = "기수 멤버십",
                priceWon = 150000,
                description = "기수형",
                thumbnailFileId = "membership-thumb",
                maxEnrollments = 20,
                coinPackageId = "coin-package-id",
                startAt = LocalDateTime.of(2026, 5, 1, 0, 0),
                endAt = LocalDateTime.of(2026, 6, 1, 0, 0),
            )
        every { productAdaptor.findVisibleCatalogProductById(product.id) } returns product
        every { enrollmentAdaptor.countLiveMembershipEnrollments(product.id) } returns 5L
        every { coinPackageAdaptor.findById("coin-package-id") } returns
            CoinPackage(
                id = "coin-package-id",
                name = "코인 패키지",
                priceWon = 100000,
                coinAmount = 80,
            )
        every { courseAdaptor.findAllCatalogCoursesByProductId(product.id) } returns emptyList()

        val result = useCase.execute(product.id)

        assertThat(result.productId).isEqualTo(product.id)
        assertThat(result.membership?.remainingSeats).isEqualTo(15L)
        assertThat(result.membership?.coinAmount).isEqualTo(80)
        assertThat(result.course).isNull()
    }

    @Test
    fun `상품이 없으면 ProductNotFoundException이 발생한다`() {
        every { productAdaptor.findVisibleCatalogProductById("missing-product") } returns null

        assertThatThrownBy { useCase.execute("missing-product") }
            .isInstanceOf(ProductNotFoundException::class.java)
    }

    private fun makeCatalogCourseDto(courseProduct: CourseProduct): CatalogCourseDto {
        val teacherUser =
            User(
                email = "teacher@test.com",
                name = "김선생",
                authProvider = AuthProvider.EMAIL,
            )

        return CatalogCourseDto(
            course =
                Course(
                    id = 1L,
                    productId = courseProduct.id,
                    teacherUserId = teacherUser.id,
                    status = CourseStatus.LISTED,
                    maxEnrollments = 5,
                ),
            courseProduct = courseProduct,
            teacher = Teacher(user = teacherUser),
            teacherUser = teacherUser,
            liveEnrollmentCount = 1L,
        )
    }
}
