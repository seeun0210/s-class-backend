package com.sclass.backoffice.membershipproduct.usecase

import com.sclass.domain.domains.coin.adaptor.CoinPackageAdaptor
import com.sclass.domain.domains.coin.domain.CoinPackage
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CourseProduct
import com.sclass.domain.domains.product.domain.MembershipProduct
import com.sclass.domain.domains.product.exception.ProductTypeMismatchException
import com.sclass.infrastructure.s3.ThumbnailUrlResolver
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetMembershipProductDetailUseCaseTest {
    private lateinit var productAdaptor: ProductAdaptor
    private lateinit var coinPackageAdaptor: CoinPackageAdaptor
    private lateinit var thumbnailUrlResolver: ThumbnailUrlResolver
    private lateinit var useCase: GetMembershipProductDetailUseCase

    @BeforeEach
    fun setUp() {
        productAdaptor = mockk()
        coinPackageAdaptor = mockk()
        thumbnailUrlResolver = mockk()
        useCase = GetMembershipProductDetailUseCase(productAdaptor, coinPackageAdaptor, thumbnailUrlResolver)
    }

    @Test
    fun `비공개 상태라도 admin은 상세를 조회할 수 있다`() {
        val product =
            MembershipProduct(
                name = "프리미엄",
                priceWon = 10000,
                periodDays = 30,
                coinPackageId = "cp-001",
            )
        val coinPackage = CoinPackage(name = "코인 1000", priceWon = 10000, coinAmount = 1000)
        every { productAdaptor.findById(product.id) } returns product
        every { coinPackageAdaptor.findById("cp-001") } returns coinPackage
        every { thumbnailUrlResolver.resolve(any()) } returns null

        val result = useCase.execute(product.id)

        assertThat(result.productId).isEqualTo(product.id)
        assertThat(result.visible).isFalse()
        assertThat(result.coinAmount).isEqualTo(1000)
    }

    @Test
    fun `MembershipProduct가 아니면 ProductTypeMismatchException이 발생한다`() {
        val courseProduct = CourseProduct(name = "수학", priceWon = 10000, totalLessons = 12)
        every { productAdaptor.findById(any()) } returns courseProduct

        assertThatThrownBy { useCase.execute("some-id") }
            .isInstanceOf(ProductTypeMismatchException::class.java)
    }
}
