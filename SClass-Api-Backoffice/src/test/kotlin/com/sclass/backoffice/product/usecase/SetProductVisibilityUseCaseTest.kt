package com.sclass.backoffice.product.usecase

import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CourseProduct
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SetProductVisibilityUseCaseTest {
    private lateinit var productAdaptor: ProductAdaptor
    private lateinit var useCase: SetProductVisibilityUseCase

    @BeforeEach
    fun setUp() {
        productAdaptor = mockk()
        useCase = SetProductVisibilityUseCase(productAdaptor)
    }

    private fun courseProduct() =
        CourseProduct(
            name = "수학 기초",
            priceWon = 300000,
            totalLessons = 12,
        )

    @Test
    fun `visible=true 이면 상품을 show 한다`() {
        val product = courseProduct()
        every { productAdaptor.findById(product.id) } returns product

        useCase.execute(product.id, visible = true)

        assertTrue(product.visible)
        verify(exactly = 1) { productAdaptor.findById(product.id) }
    }

    @Test
    fun `visible=false 이면 상품을 hide 한다`() {
        val product = courseProduct().also { it.show() }
        every { productAdaptor.findById(product.id) } returns product

        useCase.execute(product.id, visible = false)

        assertFalse(product.visible)
    }

    @Test
    fun `이미 visible=true 인 상품에 visible=true 요청은 멱등하다`() {
        val product = courseProduct().also { it.show() }
        every { productAdaptor.findById(product.id) } returns product

        useCase.execute(product.id, visible = true)

        assertTrue(product.visible)
    }

    @Test
    fun `이미 visible=false 인 상품에 visible=false 요청은 멱등하다`() {
        val product = courseProduct()
        every { productAdaptor.findById(product.id) } returns product

        useCase.execute(product.id, visible = false)

        assertFalse(product.visible)
    }
}
