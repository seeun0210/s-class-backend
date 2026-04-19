package com.sclass.backoffice.membershipproduct.usecase

import com.sclass.domain.domains.coin.adaptor.CoinPackageAdaptor
import com.sclass.domain.domains.coin.domain.CoinPackage
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.MembershipProduct
import com.sclass.infrastructure.s3.ThumbnailUrlResolver
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

class GetMembershipProductListUseCaseTest {
    private lateinit var productAdaptor: ProductAdaptor
    private lateinit var coinPackageAdaptor: CoinPackageAdaptor
    private lateinit var thumbnailUrlResolver: ThumbnailUrlResolver
    private lateinit var useCase: GetMembershipProductListUseCase

    @BeforeEach
    fun setUp() {
        productAdaptor = mockk()
        coinPackageAdaptor = mockk()
        thumbnailUrlResolver = mockk()
        useCase = GetMembershipProductListUseCase(productAdaptor, coinPackageAdaptor, thumbnailUrlResolver)
    }

    @Test
    fun `페이지 내용과 각 항목의 coinAmount가 매핑된다`() {
        val visible =
            MembershipProduct(name = "A", priceWon = 10000, periodDays = 30, coinPackageId = "cp-1").also { it.show() }
        val hidden =
            MembershipProduct(name = "B", priceWon = 20000, periodDays = 60, coinPackageId = "cp-2")
        val pageable = PageRequest.of(0, 20)
        every { productAdaptor.findAllMemberships(pageable) } returns PageImpl(listOf(visible, hidden), pageable, 2)
        every { coinPackageAdaptor.findById("cp-1") } returns CoinPackage(name = "1", priceWon = 10000, coinAmount = 1000)
        every { coinPackageAdaptor.findById("cp-2") } returns CoinPackage(name = "2", priceWon = 20000, coinAmount = 2000)
        every { thumbnailUrlResolver.resolve(any()) } returns null

        val result = useCase.execute(pageable)

        assertThat(result.content).hasSize(2)
        assertThat(result.totalElements).isEqualTo(2)
        assertThat(result.content[0].coinAmount).isEqualTo(1000)
        assertThat(result.content[0].visible).isTrue()
        assertThat(result.content[1].coinAmount).isEqualTo(2000)
        assertThat(result.content[1].visible).isFalse()
    }
}
