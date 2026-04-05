package com.sclass.domain.domains.coin.domain

import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CoinBalanceTest {
    private fun createBalance(balance: Int = 0) = CoinBalance(userId = "user-id-00000000000", balance = balance)

    @Nested
    inner class Issue {
        @Test
        fun `코인 발급 시 balance와 totalIssued가 증가한다`() {
            val coinBalance = createBalance()

            coinBalance.issue(100)

            assertAll(
                { assertEquals(100, coinBalance.balance) },
                { assertEquals(100, coinBalance.totalIssued) },
            )
        }
    }

    @Nested
    inner class Deduct {
        @Test
        fun `코인 차감 시 balance가 감소하고 totalUsed가 증가한다`() {
            val coinBalance = createBalance(balance = 100)

            coinBalance.deduct(30)

            assertAll(
                { assertEquals(70, coinBalance.balance) },
                { assertEquals(30, coinBalance.totalUsed) },
            )
        }

        @Test
        fun `잔액보다 많은 코인 차감 시 예외가 발생한다`() {
            val coinBalance = createBalance(balance = 50)

            assertThrows<IllegalStateException> {
                coinBalance.deduct(100)
            }
        }
    }

    @Nested
    inner class Refund {
        @Test
        fun `코인 환불 시 balance가 증가한다`() {
            val coinBalance = createBalance(balance = 50)

            coinBalance.refund(30)

            assertEquals(80, coinBalance.balance)
        }
    }
}
