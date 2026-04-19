package com.sclass.supporters.coin.usecase

import com.sclass.domain.domains.coin.adaptor.CoinAdaptor
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetCoinBalanceUseCaseTest {
    private lateinit var coinAdaptor: CoinAdaptor
    private lateinit var useCase: GetCoinBalanceUseCase

    @BeforeEach
    fun setUp() {
        coinAdaptor = mockk()
        useCase = GetCoinBalanceUseCase(coinAdaptor)
    }

    @Test
    fun `활성 Lot 합계를 잔액으로 반환한다`() {
        every { coinAdaptor.sumActiveLots(any(), any()) } returns 200

        val result = useCase.execute("user-id-0000000000000000000001")

        assertEquals(200, result.balance)
    }

    @Test
    fun `활성 Lot 이 없으면 0 을 반환한다`() {
        every { coinAdaptor.sumActiveLots(any(), any()) } returns 0

        val result = useCase.execute("user-id-0000000000000000000001")

        assertEquals(0, result.balance)
    }
}
