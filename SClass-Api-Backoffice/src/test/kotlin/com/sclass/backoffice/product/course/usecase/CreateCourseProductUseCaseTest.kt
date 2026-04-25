package com.sclass.backoffice.product.course.usecase

import com.sclass.backoffice.product.course.dto.CreateCourseProductRequest
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CourseProduct
import com.sclass.infrastructure.s3.ThumbnailUrlResolver
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CreateCourseProductUseCaseTest {
    private lateinit var productAdaptor: ProductAdaptor
    private lateinit var thumbnailUrlResolver: ThumbnailUrlResolver
    private lateinit var useCase: CreateCourseProductUseCase

    @BeforeEach
    fun setUp() {
        productAdaptor = mockk()
        thumbnailUrlResolver = mockk()
        every { thumbnailUrlResolver.resolve(any()) } answers {
            firstArg<String?>()?.let { "https://static.test.sclass.click/course_thumbnail/$it" }
        }
        useCase = CreateCourseProductUseCase(productAdaptor, thumbnailUrlResolver)
    }

    @Test
    fun `course product를 생성한다`() {
        every { productAdaptor.save(any()) } answers { firstArg() }

        val result =
            useCase.execute(
                CreateCourseProductRequest(
                    name = "매칭형 수학 코스",
                    description = "설명",
                    curriculum = "커리큘럼",
                    thumbnailFileId = "file-id-00000000001",
                    priceWon = 300000,
                    totalLessons = 12,
                    requiresMatching = true,
                    visible = true,
                ),
            )

        assertThat(result.name).isEqualTo("매칭형 수학 코스")
        assertThat(result.description).isEqualTo("설명")
        assertThat(result.curriculum).isEqualTo("커리큘럼")
        assertThat(result.priceWon).isEqualTo(300000)
        assertThat(result.totalLessons).isEqualTo(12)
        assertThat(result.requiresMatching).isTrue()
        assertThat(result.visible).isTrue()
        assertThat(result.thumbnailUrl).isEqualTo("https://static.test.sclass.click/course_thumbnail/file-id-00000000001")
    }

    @Test
    fun `visible이 false면 숨김 상태로 생성한다`() {
        val saved = mutableListOf<CourseProduct>()
        every { productAdaptor.save(any()) } answers {
            firstArg<CourseProduct>().also(saved::add)
        }

        val result =
            useCase.execute(
                CreateCourseProductRequest(
                    name = "일반 수학 코스",
                    priceWon = 250000,
                    totalLessons = 8,
                ),
            )

        assertThat(saved.single().visible).isFalse()
        assertThat(result.visible).isFalse()
        assertThat(result.requiresMatching).isFalse()
    }
}
