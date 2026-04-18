package com.sclass.domain.domains.product.domain

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ProductTest {
    private fun newProduct() =
        CourseProduct(
            name = "수학 기초",
            priceWon = 300000,
            totalLessons = 12,
        )

    @Test
    fun `신규 상품은 기본적으로 비공개 상태이다`() {
        assertFalse(newProduct().visible)
    }

    @Test
    fun `show 를 호출하면 visible 이 true 가 된다`() {
        val product = newProduct()

        product.show()

        assertTrue(product.visible)
    }

    @Test
    fun `hide 를 호출하면 visible 이 false 가 된다`() {
        val product = newProduct().also { it.show() }

        product.hide()

        assertFalse(product.visible)
    }

    @Test
    fun `show 는 멱등하다`() {
        val product = newProduct()

        product.show()
        product.show()

        assertTrue(product.visible)
    }

    @Test
    fun `hide 는 멱등하다`() {
        val product = newProduct()

        product.hide()
        product.hide()

        assertFalse(product.visible)
    }
}
