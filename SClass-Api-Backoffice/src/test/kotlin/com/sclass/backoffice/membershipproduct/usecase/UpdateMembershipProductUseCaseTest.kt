package com.sclass.backoffice.membershipproduct.usecase

import com.sclass.backoffice.membershipproduct.dto.UpdateMembershipProductRequest
import com.sclass.domain.domains.coin.adaptor.CoinPackageAdaptor
import com.sclass.domain.domains.coin.domain.CoinPackage
import com.sclass.domain.domains.coin.exception.CoinPackageNotFoundException
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CourseProduct
import com.sclass.domain.domains.product.domain.MembershipProduct
import com.sclass.domain.domains.product.exception.ProductTypeMismatchException
import com.sclass.infrastructure.s3.ThumbnailUrlResolver
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UpdateMembershipProductUseCaseTest {
    private lateinit var productAdaptor: ProductAdaptor
    private lateinit var coinPackageAdaptor: CoinPackageAdaptor
    private lateinit var thumbnailUrlResolver: ThumbnailUrlResolver
    private lateinit var useCase: UpdateMembershipProductUseCase

    @BeforeEach
    fun setUp() {
        productAdaptor = mockk()
        coinPackageAdaptor = mockk()
        thumbnailUrlResolver = mockk()
        useCase = UpdateMembershipProductUseCase(productAdaptor, coinPackageAdaptor, thumbnailUrlResolver)
    }

    private fun existing() =
        MembershipProduct(
            name = "기존",
            priceWon = 5000,
            periodDays = 30,
            maxEnrollments = 50,
            coinPackageId = "cp-old",
        )

    @Test
    fun `부분 업데이트가 반영된다`() {
        val product = existing()
        val coinPackage = CoinPackage(name = "코인 1000", priceWon = 10000, coinAmount = 1000)
        every { productAdaptor.findById(product.id) } returns product
        every { coinPackageAdaptor.findById("cp-old") } returns coinPackage
        every { thumbnailUrlResolver.resolve(any()) } returns null

        val result =
            useCase.execute(
                product.id,
                UpdateMembershipProductRequest(
                    name = "업데이트",
                    priceWon = 15000,
                    periodDays = 60,
                ),
            )

        assertThat(result.name).isEqualTo("업데이트")
        assertThat(result.priceWon).isEqualTo(15000)
        assertThat(result.periodDays).isEqualTo(60)
        assertThat(result.maxEnrollments).isEqualTo(50)
        assertThat(result.coinPackageId).isEqualTo("cp-old")
    }

    @Test
    fun `coinPackageId 변경 시 새 coinPackage 존재 여부를 검증한다`() {
        val product = existing()
        val newCoinPackage = CoinPackage(name = "코인 2000", priceWon = 20000, coinAmount = 2000)
        every { productAdaptor.findById(product.id) } returns product
        every { coinPackageAdaptor.findById("cp-new") } returns newCoinPackage
        every { thumbnailUrlResolver.resolve(any()) } returns null

        val result =
            useCase.execute(
                product.id,
                UpdateMembershipProductRequest(coinPackageId = "cp-new"),
            )

        assertThat(result.coinPackageId).isEqualTo("cp-new")
        assertThat(result.coinAmount).isEqualTo(2000)
        verify(atLeast = 1) { coinPackageAdaptor.findById("cp-new") }
    }

    @Test
    fun `새 coinPackage가 없으면 CoinPackageNotFoundException이 발생한다`() {
        val product = existing()
        every { productAdaptor.findById(product.id) } returns product
        every { coinPackageAdaptor.findById("cp-missing") } throws CoinPackageNotFoundException()

        assertThatThrownBy {
            useCase.execute(
                product.id,
                UpdateMembershipProductRequest(coinPackageId = "cp-missing"),
            )
        }.isInstanceOf(CoinPackageNotFoundException::class.java)
    }

    @Test
    fun `MembershipProduct가 아니면 ProductTypeMismatchException이 발생한다`() {
        val courseProduct = CourseProduct(name = "수학", priceWon = 10000, totalLessons = 12)
        every { productAdaptor.findById(any()) } returns courseProduct

        assertThatThrownBy {
            useCase.execute("some-id", UpdateMembershipProductRequest(name = "x"))
        }.isInstanceOf(ProductTypeMismatchException::class.java)
    }
}
