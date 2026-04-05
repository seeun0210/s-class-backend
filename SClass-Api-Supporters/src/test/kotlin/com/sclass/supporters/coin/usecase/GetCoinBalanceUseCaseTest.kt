package com.sclass.supporters.coin.usecase

import com.sclass.domain.domains.coin.adaptor.CoinAdaptor
import com.sclass.domain.domains.coin.domain.CoinBalance
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertAll
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
    fun `코인 잔액이 있으면 실제 값을 반환한다`() {
        val balance =
            CoinBalance(userId = "user-id-0000000000000000000001").apply {
                issue(300)
                deduct(100)
            }
        every { coinAdaptor.findBalanceByUserIdOrNull(any()) } returns balance

        val result = useCase.execute("user-id-0000000000000000000001")

        assertAll(
            { assertEquals(200, result.balance) },
            { assertEquals(300, result.totalIssued) },
            { assertEquals(100, result.totalUsed) },
        )
    }

    @Test
    fun `코인 잔액이 없으면 0을 반환한다`() {
        every { coinAdaptor.findBalanceByUserIdOrNull(any()) } returns null

        val result = useCase.execute("user-id-0000000000000000000001")

        assertAll(
            { assertEquals(0, result.balance) },
            { assertEquals(0, result.totalIssued) },
            { assertEquals(0, result.totalUsed) },
        )
    }
}
