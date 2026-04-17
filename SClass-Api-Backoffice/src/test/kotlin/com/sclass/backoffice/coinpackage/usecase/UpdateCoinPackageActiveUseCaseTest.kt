package com.sclass.backoffice.coinpackage.usecase

import com.sclass.domain.domains.coin.adaptor.CoinPackageAdaptor
import com.sclass.domain.domains.coin.domain.CoinPackage
import com.sclass.domain.domains.coin.exception.CoinPackageNotFoundException
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UpdateCoinPackageActiveUseCaseTest {
    private val coinPackageAdaptor = mockk<CoinPackageAdaptor>()
    private val useCase = UpdateCoinPackageActiveUseCase(coinPackageAdaptor)

    private fun coinPackage(active: Boolean) =
        CoinPackage(
            name = "10,000 코인",
            priceWon = 10_000,
            coinAmount = 10_000,
            active = active,
        )

    @Nested
    inner class Execute {
        @Test
        fun `active=false 로 호출하면 패키지를 비활성화한다`() {
            val target = coinPackage(active = true)
            every { coinPackageAdaptor.findById(target.id) } returns target

            useCase.execute(target.id, active = false)

            assertFalse(target.active)
        }

        @Test
        fun `active=true 로 호출하면 비활성 패키지를 다시 활성화한다`() {
            val target = coinPackage(active = false)
            every { coinPackageAdaptor.findById(target.id) } returns target

            useCase.execute(target.id, active = true)

            assertTrue(target.active)
        }

        @Test
        fun `존재하지 않는 패키지면 CoinPackageNotFoundException 을 던진다`() {
            every { coinPackageAdaptor.findById(any()) } throws CoinPackageNotFoundException()

            assertThrows(CoinPackageNotFoundException::class.java) {
                useCase.execute("non-existent-id", active = false)
            }
        }
    }
}
