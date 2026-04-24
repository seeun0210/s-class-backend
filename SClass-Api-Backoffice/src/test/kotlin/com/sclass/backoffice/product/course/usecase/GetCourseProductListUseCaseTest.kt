package com.sclass.backoffice.product.course.usecase

import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CourseProduct
import com.sclass.infrastructure.s3.ThumbnailUrlResolver
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

class GetCourseProductListUseCaseTest {
    private lateinit var productAdaptor: ProductAdaptor
    private lateinit var thumbnailUrlResolver: ThumbnailUrlResolver
    private lateinit var useCase: GetCourseProductListUseCase

    @BeforeEach
    fun setUp() {
        productAdaptor = mockk()
        thumbnailUrlResolver = mockk()
        every { thumbnailUrlResolver.resolve(any()) } answers {
            firstArg<String?>()?.let { "https://static.test.sclass.click/course_thumbnail/$it" }
        }
        useCase = GetCourseProductListUseCase(productAdaptor, thumbnailUrlResolver)
    }

    @Test
    fun `course product 목록을 페이지로 반환한다`() {
        val pageable = PageRequest.of(0, 20)
        every { productAdaptor.findCourseProducts(pageable) } returns
            PageImpl(
                listOf(
                    CourseProduct(
                        name = "코스 A",
                        priceWon = 100000,
                        totalLessons = 8,
                        thumbnailFileId = "file-a",
                    ),
                    CourseProduct(
                        name = "코스 B",
                        priceWon = 200000,
                        totalLessons = 12,
                    ),
                ),
                pageable,
                2,
            )

        val result = useCase.execute(pageable)

        assertThat(result.totalElements).isEqualTo(2)
        assertThat(result.totalPages).isEqualTo(1)
        assertThat(result.currentPage).isEqualTo(0)
        assertThat(result.content).hasSize(2)
        assertThat(result.content[0].thumbnailUrl).isEqualTo("https://static.test.sclass.click/course_thumbnail/file-a")
        assertThat(result.content[1].thumbnailUrl).isNull()
    }
}
