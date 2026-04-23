package com.sclass.supporters.catalog.controller

import com.sclass.domain.domains.course.repository.CourseRepository
import com.sclass.domain.domains.product.domain.CourseProduct
import com.sclass.domain.domains.product.repository.ProductRepository
import com.sclass.supporters.config.ApiIntegrationTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ApiIntegrationTest
class CatalogControllerIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

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
    fun `pageable sort와 catalogSort를 함께 보내도 product 목록이 조회된다`() {
        val product =
            productRepository.saveAndFlush(
                CourseProduct(
                    name = "매칭형 코스",
                    priceWon = 120000,
                    totalLessons = 10,
                    requiresMatching = true,
                ).also { it.show() },
            )

        mockMvc
            .perform(
                get("/api/v1/catalog/products")
                    .param("sort", "createdAt,desc")
                    .param("catalogSort", "LATEST"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content[0].productId").value(product.id))
    }
}
