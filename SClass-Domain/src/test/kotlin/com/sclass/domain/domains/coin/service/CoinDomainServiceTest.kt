package com.sclass.domain.domains.coin.service

import com.sclass.domain.domains.coin.adaptor.CoinAdaptor
import com.sclass.domain.domains.coin.domain.CoinBalance
import com.sclass.domain.domains.coin.domain.CoinTransaction
import com.sclass.domain.domains.coin.domain.CoinTransactionType
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CoinDomainServiceTest {
    private lateinit var coinAdaptor: CoinAdaptor
    private lateinit var coinDomainService: CoinDomainService

    @BeforeEach
    fun setUp() {
        coinAdaptor = mockk()
        coinDomainService = CoinDomainService(coinAdaptor)
    }

    @Nested
    inner class Issue {
        @Test
        fun `기존 잔액이 없으면 새로 생성하고 코인을 발급한다`() {
            val balanceSlot = slot<CoinBalance>()
            val transactionSlot = slot<CoinTransaction>()
            every { coinAdaptor.findBalanceByUserIdOrNull("user-01") } returns null
            every { coinAdaptor.saveBalance(capture(balanceSlot)) } answers { balanceSlot.captured }
            every { coinAdaptor.saveTransaction(capture(transactionSlot)) } answers { transactionSlot.captured }

            coinDomainService.issue("user-01", 100, "payment-01")

            assertAll(
                { assertEquals(100, balanceSlot.captured.balance) },
                { assertEquals(CoinTransactionType.ISSUED, transactionSlot.captured.type) },
                { assertEquals(100, transactionSlot.captured.amount) },
                { assertEquals("payment-01", transactionSlot.captured.referenceId) },
            )
        }

        @Test
        fun `기존 잔액이 있으면 코인을 추가 발급한다`() {
            val existingBalance = CoinBalance(userId = "user-01", balance = 50)
            val balanceSlot = slot<CoinBalance>()
            val transactionSlot = slot<CoinTransaction>()
            every { coinAdaptor.findBalanceByUserIdOrNull("user-01") } returns existingBalance
            every { coinAdaptor.saveBalance(capture(balanceSlot)) } answers { balanceSlot.captured }
            every { coinAdaptor.saveTransaction(capture(transactionSlot)) } answers { transactionSlot.captured }

            coinDomainService.issue("user-01", 100)

            assertEquals(150, balanceSlot.captured.balance)
        }
    }

    @Nested
    inner class Deduct {
        @Test
        fun `잔액이 충분하면 코인을 차감하고 트랜잭션을 저장한다`() {
            val existingBalance = CoinBalance(userId = "user-01", balance = 100)
            val balanceSlot = slot<CoinBalance>()
            val transactionSlot = slot<CoinTransaction>()
            every { coinAdaptor.findBalanceByUserId("user-01") } returns existingBalance
            every { coinAdaptor.saveBalance(capture(balanceSlot)) } answers { balanceSlot.captured }
            every { coinAdaptor.saveTransaction(capture(transactionSlot)) } answers { transactionSlot.captured }

            coinDomainService.deduct("user-01", 30)

            assertAll(
                { assertEquals(70, balanceSlot.captured.balance) },
                { assertEquals(CoinTransactionType.DEDUCTED, transactionSlot.captured.type) },
            )
        }
    }

    @Nested
    inner class Refund {
        @Test
        fun `환불 시 코인이 복구되고 REFUNDED 트랜잭션이 저장된다`() {
            val existingBalance = CoinBalance(userId = "user-01", balance = 50)
            val balanceSlot = slot<CoinBalance>()
            val transactionSlot = slot<CoinTransaction>()
            every { coinAdaptor.findBalanceByUserId("user-01") } returns existingBalance
            every { coinAdaptor.saveBalance(capture(balanceSlot)) } answers { balanceSlot.captured }
            every { coinAdaptor.saveTransaction(capture(transactionSlot)) } answers { transactionSlot.captured }

            coinDomainService.refund("user-01", 30, "payment-01")

            assertAll(
                { assertEquals(80, balanceSlot.captured.balance) },
                { assertEquals(CoinTransactionType.REFUNDED, transactionSlot.captured.type) },
                { assertEquals("payment-01", transactionSlot.captured.referenceId) },
            )
        }
    }
}
