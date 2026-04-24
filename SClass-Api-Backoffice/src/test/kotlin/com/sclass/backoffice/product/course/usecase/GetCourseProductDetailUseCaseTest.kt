package com.sclass.backoffice.product.course.usecase

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

class GetCourseProductDetailUseCaseTest {
    private lateinit var productAdaptor: ProductAdaptor
    private lateinit var thumbnailUrlResolver: ThumbnailUrlResolver
    private lateinit var useCase: GetCourseProductDetailUseCase

    @BeforeEach
    fun setUp() {
        productAdaptor = mockk()
        thumbnailUrlResolver = mockk()
        every { thumbnailUrlResolver.resolve(any()) } answers {
            firstArg<String?>()?.let { "https://static.test.sclass.click/course_thumbnail/$it" }
        }
        useCase = GetCourseProductDetailUseCase(productAdaptor, thumbnailUrlResolver)
    }

    @Test
    fun `course product 상세를 반환한다`() {
        val product =
            CourseProduct(
                name = "수학 코스",
                priceWon = 300000,
                totalLessons = 12,
                curriculum = "커리큘럼",
                thumbnailFileId = "file-id",
                requiresMatching = true,
            )
        every { productAdaptor.findCourseProductById(product.id) } returns product

        val result = useCase.execute(product.id)

        assertThat(result.productId).isEqualTo(product.id)
        assertThat(result.curriculum).isEqualTo("커리큘럼")
        assertThat(result.requiresMatching).isTrue()
        assertThat(result.thumbnailUrl).isEqualTo("https://static.test.sclass.click/course_thumbnail/file-id")
    }

    @Test
    fun `course product가 아니면 예외가 발생한다`() {
        every { productAdaptor.findCourseProductById("product-id-00000000001") } throws ProductTypeMismatchException()

        assertThatThrownBy {
            useCase.execute("product-id-00000000001")
        }.isInstanceOf(ProductTypeMismatchException::class.java)
    }

    @Test
    fun `상품이 존재하지 않으면 not found 예외가 발생한다`() {
        every { productAdaptor.findCourseProductById("product-id-00000000001") } throws ProductNotFoundException()

        assertThatThrownBy {
            useCase.execute("product-id-00000000001")
        }.isInstanceOf(ProductNotFoundException::class.java)
    }
}
