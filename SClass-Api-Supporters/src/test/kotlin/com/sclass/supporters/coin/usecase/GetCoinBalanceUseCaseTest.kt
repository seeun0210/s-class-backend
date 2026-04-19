package com.sclass.supporters.coin.usecase

import com.sclass.domain.domains.coin.adaptor.CoinAdaptor
import com.sclass.domain.domains.coin.domain.CoinLot
import com.sclass.domain.domains.coin.domain.CoinLotSourceType
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class GetCoinBalanceUseCaseTest {
    private lateinit var coinAdaptor: CoinAdaptor
    private lateinit var useCase: GetCoinBalanceUseCase

    @BeforeEach
    fun setUp() {
        coinAdaptor = mockk()
        useCase = GetCoinBalanceUseCase(coinAdaptor)
    }

    private fun makeLot(
        remaining: Int,
        expireAt: LocalDateTime? = null,
    ) = CoinLot(
        userId = "user-id-0000000000000000000001",
        amount = remaining,
        remaining = remaining,
        expireAt = expireAt,
        sourceType = CoinLotSourceType.PURCHASE,
    )

    @Test
    fun `활성 Lot 합계를 잔액으로 반환하고 lot 목록을 포함한다`() {
        val expireAt = LocalDateTime.of(2026, 6, 30, 0, 0)
        val lots = listOf(makeLot(300, expireAt), makeLot(200))
        every { coinAdaptor.findActiveLots(any(), any()) } returns lots

        val result = useCase.execute("user-id-0000000000000000000001")

        assertAll(
            { assertEquals(500, result.balance) },
            { assertEquals(2, result.lots.size) },
            { assertEquals(300, result.lots[0].remaining) },
            { assertEquals(expireAt, result.lots[0].expireAt) },
            { assertEquals(200, result.lots[1].remaining) },
        )
    }

    @Test
    fun `활성 Lot이 없으면 잔액 0에 빈 목록을 반환한다`() {
        every { coinAdaptor.findActiveLots(any(), any()) } returns emptyList()

        val result = useCase.execute("user-id-0000000000000000000001")

        assertAll(
            { assertEquals(0, result.balance) },
            { assertEquals(0, result.lots.size) },
        )
    }
}
