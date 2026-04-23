package com.sclass.backoffice.product.course.usecase

import com.sclass.backoffice.product.course.dto.UpdateCourseProductRequest
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
    private lateinit var thumbnailUrlResolver: ThumbnailUrlResolver
    private lateinit var useCase: UpdateCourseProductUseCase

    @BeforeEach
    fun setUp() {
        productAdaptor = mockk()
        thumbnailUrlResolver = mockk()
        every { thumbnailUrlResolver.resolve(any()) } answers {
            firstArg<String?>()?.let { "https://static.test.sclass.click/course_thumbnail/$it" }
        }
        useCase = UpdateCourseProductUseCase(productAdaptor, thumbnailUrlResolver)
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
}
