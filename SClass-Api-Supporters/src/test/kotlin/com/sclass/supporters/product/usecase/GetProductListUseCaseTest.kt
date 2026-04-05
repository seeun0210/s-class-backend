package com.sclass.supporters.product.usecase

import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CoinProduct
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetProductListUseCaseTest {
    private lateinit var productAdaptor: ProductAdaptor
    private lateinit var useCase: GetProductListUseCase

    @BeforeEach
    fun setUp() {
        productAdaptor = mockk()
        useCase = GetProductListUseCase(productAdaptor)
    }

    @Test
    fun `활성 코인 상품 목록을 반환한다`() {
        every { productAdaptor.findAllActiveCoinProducts() } returns
            listOf(
                CoinProduct(name = "코인 100", priceWon = 1000, coinAmount = 100),
                CoinProduct(name = "코인 200", priceWon = 1800, coinAmount = 200),
            )

        val result = useCase.execute()

        assertEquals(2, result.products.size)
    }
}
