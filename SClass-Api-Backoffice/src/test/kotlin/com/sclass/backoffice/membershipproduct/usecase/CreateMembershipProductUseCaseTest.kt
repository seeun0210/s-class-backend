package com.sclass.backoffice.membershipproduct.usecase

import com.sclass.backoffice.membershipproduct.dto.CreateMembershipProductRequest
import com.sclass.domain.domains.coin.adaptor.CoinPackageAdaptor
import com.sclass.domain.domains.coin.domain.CoinPackage
import com.sclass.domain.domains.coin.exception.CoinPackageNotFoundException
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.MembershipProduct
import com.sclass.infrastructure.s3.ThumbnailUrlResolver
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CreateMembershipProductUseCaseTest {
    private lateinit var productAdaptor: ProductAdaptor
    private lateinit var coinPackageAdaptor: CoinPackageAdaptor
    private lateinit var thumbnailUrlResolver: ThumbnailUrlResolver
    private lateinit var useCase: CreateMembershipProductUseCase

    @BeforeEach
    fun setUp() {
        productAdaptor = mockk()
        coinPackageAdaptor = mockk()
        thumbnailUrlResolver = mockk()
        useCase = CreateMembershipProductUseCase(productAdaptor, coinPackageAdaptor, thumbnailUrlResolver)
    }

    @Test
    fun `MembershipProduct가 생성되고 coinAmount가 응답에 포함된다`() {
        val coinPackage = CoinPackage(name = "코인 1000", priceWon = 10000, coinAmount = 1000)
        val captured = slot<MembershipProduct>()
        every { coinPackageAdaptor.findById("cp-001") } returns coinPackage
        every { productAdaptor.save(capture(captured)) } answers { captured.captured }
        every { thumbnailUrlResolver.resolve(any()) } returns null

        val result =
            useCase.execute(
                CreateMembershipProductRequest(
                    name = "프리미엄",
                    priceWon = 10000,
                    periodDays = 30,
                    maxEnrollments = 100,
                    coinPackageId = "cp-001",
                ),
            )

        assertThat(result.name).isEqualTo("프리미엄")
        assertThat(result.periodDays).isEqualTo(30)
        assertThat(result.maxEnrollments).isEqualTo(100)
        assertThat(result.coinPackageId).isEqualTo("cp-001")
        assertThat(result.coinAmount).isEqualTo(1000)
        assertThat(captured.captured.priceWon).isEqualTo(10000)
    }

    @Test
    fun `coinPackageId가 존재하지 않으면 CoinPackageNotFoundException이 발생한다`() {
        every { coinPackageAdaptor.findById("cp-missing") } throws CoinPackageNotFoundException()

        assertThatThrownBy {
            useCase.execute(
                CreateMembershipProductRequest(
                    name = "프리미엄",
                    priceWon = 10000,
                    periodDays = 30,
                    coinPackageId = "cp-missing",
                ),
            )
        }.isInstanceOf(CoinPackageNotFoundException::class.java)
    }
}
