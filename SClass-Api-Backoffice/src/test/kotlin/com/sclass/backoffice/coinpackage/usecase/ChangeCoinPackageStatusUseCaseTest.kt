package com.sclass.backoffice.coinpackage.usecase

import com.sclass.domain.domains.coin.adaptor.CoinPackageAdaptor
import com.sclass.domain.domains.coin.domain.CoinPackage
import com.sclass.domain.domains.coin.domain.CoinPackageStatus
import com.sclass.domain.domains.coin.exception.CoinPackageInvalidStatusTransitionException
import com.sclass.domain.domains.coin.exception.CoinPackageNotFoundException
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ChangeCoinPackageStatusUseCaseTest {
    private val coinPackageAdaptor = mockk<CoinPackageAdaptor>()
    private val useCase = ChangeCoinPackageStatusUseCase(coinPackageAdaptor)

    private fun coinPackage(status: CoinPackageStatus) =
        CoinPackage(
            name = "10,000 코인",
            priceWon = 10_000,
            coinAmount = 10_000,
            status = status,
        )

    @Nested
    inner class Execute {
        @Test
        fun `INACTIVE로 전이하면 패키지가 INACTIVE가 된다`() {
            val target = coinPackage(status = CoinPackageStatus.ACTIVE)
            every { coinPackageAdaptor.findById(target.id) } returns target

            useCase.execute(target.id, CoinPackageStatus.INACTIVE)

            assertEquals(CoinPackageStatus.INACTIVE, target.status)
        }

        @Test
        fun `ACTIVE로 전이하면 INACTIVE 패키지를 다시 활성화한다`() {
            val target = coinPackage(status = CoinPackageStatus.INACTIVE)
            every { coinPackageAdaptor.findById(target.id) } returns target

            useCase.execute(target.id, CoinPackageStatus.ACTIVE)

            assertEquals(CoinPackageStatus.ACTIVE, target.status)
        }

        @Test
        fun `ARCHIVED로 전이하면 패키지를 영구 단종한다`() {
            val target = coinPackage(status = CoinPackageStatus.ACTIVE)
            every { coinPackageAdaptor.findById(target.id) } returns target

            useCase.execute(target.id, CoinPackageStatus.ARCHIVED)

            assertEquals(CoinPackageStatus.ARCHIVED, target.status)
        }

        @Test
        fun `이미 ACTIVE 인 패키지에 ACTIVE 전이를 요청해도 멱등하게 성공한다`() {
            val target = coinPackage(status = CoinPackageStatus.ACTIVE)
            every { coinPackageAdaptor.findById(target.id) } returns target

            useCase.execute(target.id, CoinPackageStatus.ACTIVE)

            assertEquals(CoinPackageStatus.ACTIVE, target.status)
        }

        @Test
        fun `ARCHIVED 패키지를 ACTIVE로 되돌리려 하면 예외가 발생한다`() {
            val target = coinPackage(status = CoinPackageStatus.ARCHIVED)
            every { coinPackageAdaptor.findById(target.id) } returns target

            assertThatThrownBy {
                useCase.execute(target.id, CoinPackageStatus.ACTIVE)
            }.isInstanceOf(CoinPackageInvalidStatusTransitionException::class.java)
        }

        @Test
        fun `존재하지 않는 패키지면 CoinPackageNotFoundException 을 던진다`() {
            every { coinPackageAdaptor.findById(any()) } throws CoinPackageNotFoundException()

            assertThrows(CoinPackageNotFoundException::class.java) {
                useCase.execute("non-existent-id", CoinPackageStatus.INACTIVE)
            }
        }
    }
}
