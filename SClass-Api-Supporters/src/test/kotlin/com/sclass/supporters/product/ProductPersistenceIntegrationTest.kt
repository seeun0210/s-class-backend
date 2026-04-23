package com.sclass.supporters.product

import com.sclass.domain.domains.product.domain.RollingMembershipProduct
import com.sclass.domain.domains.product.repository.ProductRepository
import com.sclass.supporters.config.ApiIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@ApiIntegrationTest
class ProductPersistenceIntegrationTest {
    @Autowired
    private lateinit var productRepository: ProductRepository

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

        productRepository.delete(product)
    }
}
