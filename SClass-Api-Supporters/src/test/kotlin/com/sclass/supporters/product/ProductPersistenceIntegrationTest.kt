package com.sclass.supporters.product

import com.sclass.domain.domains.course.domain.Course
import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.domain.domains.course.repository.CourseRepository
import com.sclass.domain.domains.product.domain.CourseProduct
import com.sclass.domain.domains.product.domain.ProductCatalogSort
import com.sclass.domain.domains.product.domain.ProductType
import com.sclass.domain.domains.product.domain.RollingMembershipProduct
import com.sclass.domain.domains.product.repository.ProductRepository
import com.sclass.supporters.config.ApiIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest

@ApiIntegrationTest
class ProductPersistenceIntegrationTest {
    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var courseRepository: CourseRepository

    @AfterEach
    fun tearDown() {
        courseRepository.deleteAll()
        productRepository.deleteAll()
    }

    @Test
    fun `rolling membership product를 저장할 수 있다`() {
        val product =
            productRepository.saveAndFlush(
                RollingMembershipProduct(
                    name = "프리미엄 멤버십",
                    priceWon = 50000,
                    periodDays = 30,
                    coinPackageId = "coin-package-id-0000000001",
                ),
            )

        assertThat(product.id).isNotBlank()
    }

    @Test
    fun `catalog 목록은 열려 있는 일반 코스와 매칭형 코스 상품만 노출한다`() {
        val regularWithoutCourse =
            productRepository.saveAndFlush(
                CourseProduct(
                    name = "일반 코스",
                    priceWon = 100000,
                    totalLessons = 8,
                ).also { it.show() },
            )
        val matchingWithoutCourse =
            productRepository.saveAndFlush(
                CourseProduct(
                    name = "매칭형 코스",
                    priceWon = 120000,
                    totalLessons = 10,
                    requiresMatching = true,
                ).also { it.show() },
            )
        val regularWithListedCourse =
            productRepository.saveAndFlush(
                CourseProduct(
                    name = "모집중 코스",
                    priceWon = 130000,
                    totalLessons = 12,
                ).also { it.show() },
            )
        courseRepository.saveAndFlush(
            Course(
                productId = regularWithListedCourse.id,
                teacherUserId = "teacher-01",
                status = CourseStatus.LISTED,
                maxEnrollments = 5,
            ),
        )

        val result =
            productRepository.findVisibleCatalogProducts(
                types = listOf(ProductType.COURSE),
                sort = ProductCatalogSort.LATEST,
                pageable = PageRequest.of(0, 20),
            )

        assertThat(result.content.map { it.id })
            .contains(matchingWithoutCourse.id, regularWithListedCourse.id)
            .doesNotContain(regularWithoutCourse.id)
    }
}
