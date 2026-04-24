package com.sclass.backoffice.product.course.usecase

import com.sclass.backoffice.product.course.dto.UpdateCourseProductRequest
import com.sclass.domain.domains.course.adaptor.CourseAdaptor
import com.sclass.domain.domains.course.exception.CourseMatchingProductHasPendingMatchEnrollmentException
import com.sclass.domain.domains.course.exception.CourseMatchingProductNotConvertibleException
import com.sclass.domain.domains.course.exception.CourseProductHasPendingPaymentEnrollmentException
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CourseProduct
import com.sclass.domain.domains.product.exception.ProductNotFoundException
import com.sclass.domain.domains.product.exception.ProductTypeMismatchException
import com.sclass.infrastructure.s3.ThumbnailUrlResolver
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UpdateCourseProductUseCaseTest {
    private lateinit var productAdaptor: ProductAdaptor
    private lateinit var courseAdaptor: CourseAdaptor
    private lateinit var enrollmentAdaptor: EnrollmentAdaptor
    private lateinit var thumbnailUrlResolver: ThumbnailUrlResolver
    private lateinit var useCase: UpdateCourseProductUseCase

    @BeforeEach
    fun setUp() {
        productAdaptor = mockk()
        courseAdaptor = mockk()
        enrollmentAdaptor = mockk()
        thumbnailUrlResolver = mockk()
        every { thumbnailUrlResolver.resolve(any()) } answers {
            firstArg<String?>()?.let { "https://static.test.sclass.click/course_thumbnail/$it" }
        }
        useCase = UpdateCourseProductUseCase(productAdaptor, courseAdaptor, enrollmentAdaptor, thumbnailUrlResolver)
    }

    @Test
    fun `course product의 catalog와 fulfillment 정보를 수정한다`() {
        val product =
            CourseProduct(
                name = "기존 코스",
                priceWon = 200000,
                totalLessons = 8,
                description = "기존 설명",
                curriculum = "기존 커리큘럼",
                thumbnailFileId = "old-file-id",
            )
        every { productAdaptor.findCourseProductById(product.id) } returns product
        every { enrollmentAdaptor.hasPendingUnassignedMatchingEnrollment(product.id) } returns false
        every { enrollmentAdaptor.hasPendingAssignedRegularEnrollment(product.id) } returns false
        every { courseAdaptor.findAllByProductId(product.id) } returns emptyList()

        val result =
            useCase.execute(
                product.id,
                UpdateCourseProductRequest(
                    name = "새 코스",
                    description = "새 설명",
                    curriculum = "새 커리큘럼",
                    thumbnailFileId = "new-file-id",
                    priceWon = 300000,
                    totalLessons = 12,
                    requiresMatching = true,
                    visible = true,
                ),
            )

        assertThat(product.name).isEqualTo("새 코스")
        assertThat(product.description).isEqualTo("새 설명")
        assertThat(product.curriculum).isEqualTo("새 커리큘럼")
        assertThat(product.thumbnailFileId).isEqualTo("new-file-id")
        assertThat(product.priceWon).isEqualTo(300000)
        assertThat(product.totalLessons).isEqualTo(12)
        assertThat(product.requiresMatching).isTrue()
        assertThat(product.visible).isTrue()
        assertThat(result.thumbnailUrl).isEqualTo("https://static.test.sclass.click/course_thumbnail/new-file-id")
    }

    @Test
    fun `course product가 아니면 예외가 발생한다`() {
        every { productAdaptor.findCourseProductById("product-id-00000000001") } throws ProductTypeMismatchException()

        assertThatThrownBy {
            useCase.execute("product-id-00000000001", UpdateCourseProductRequest(name = "변경"))
        }.isInstanceOf(ProductTypeMismatchException::class.java)
    }

    @Test
    fun `상품이 존재하지 않으면 not found 예외가 발생한다`() {
        every { productAdaptor.findCourseProductById("product-id-00000000001") } throws ProductNotFoundException()

        assertThatThrownBy {
            useCase.execute("product-id-00000000001", UpdateCourseProductRequest(name = "변경"))
        }.isInstanceOf(ProductNotFoundException::class.java)
    }

    @Test
    fun `여러 코스에 연결된 매칭형 상품은 일반형으로 전환할 수 없다`() {
        val product =
            CourseProduct(
                name = "매칭형 코스",
                priceWon = 300000,
                totalLessons = 12,
                requiresMatching = true,
            )
        every { productAdaptor.findCourseProductById(product.id) } returns product
        every { enrollmentAdaptor.hasPendingUnassignedMatchingEnrollment(product.id) } returns false
        every { enrollmentAdaptor.hasPendingAssignedRegularEnrollment(product.id) } returns false
        every { courseAdaptor.findAllByProductId(product.id) } returns listOf(mockk(), mockk())

        assertThatThrownBy {
            useCase.execute(
                product.id,
                UpdateCourseProductRequest(requiresMatching = false),
            )
        }.isInstanceOf(CourseMatchingProductNotConvertibleException::class.java)
        assertThat(product.requiresMatching).isTrue()
    }

    @Test
    fun `코스가 아직 배정되지 않은 enrollment가 있으면 매칭형 상품을 일반형으로 전환할 수 없다`() {
        val product =
            CourseProduct(
                name = "매칭형 코스",
                priceWon = 300000,
                totalLessons = 12,
                requiresMatching = true,
            )
        every { productAdaptor.findCourseProductById(product.id) } returns product
        every { enrollmentAdaptor.hasPendingUnassignedMatchingEnrollment(product.id) } returns true
        every { enrollmentAdaptor.hasPendingAssignedRegularEnrollment(product.id) } returns false

        assertThatThrownBy {
            useCase.execute(
                product.id,
                UpdateCourseProductRequest(requiresMatching = false),
            )
        }.isInstanceOf(CourseMatchingProductHasPendingMatchEnrollmentException::class.java)
        assertThat(product.requiresMatching).isTrue()
    }

    @Test
    fun `결제 대기 중인 일반 enrollment가 있으면 일반형 상품을 매칭형으로 전환할 수 없다`() {
        val product =
            CourseProduct(
                name = "일반 코스",
                priceWon = 300000,
                totalLessons = 12,
                requiresMatching = false,
            )
        every { productAdaptor.findCourseProductById(product.id) } returns product
        every { enrollmentAdaptor.hasPendingAssignedRegularEnrollment(product.id) } returns true

        assertThatThrownBy {
            useCase.execute(
                product.id,
                UpdateCourseProductRequest(requiresMatching = true),
            )
        }.isInstanceOf(CourseProductHasPendingPaymentEnrollmentException::class.java)
        assertThat(product.requiresMatching).isFalse()
    }
}
