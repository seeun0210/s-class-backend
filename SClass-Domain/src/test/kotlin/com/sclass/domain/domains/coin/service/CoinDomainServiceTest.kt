package com.sclass.domain.domains.coin.service

import com.sclass.domain.domains.coin.adaptor.CoinAdaptor
import com.sclass.domain.domains.coin.domain.CoinLot
import com.sclass.domain.domains.coin.domain.CoinLotSourceType
import com.sclass.domain.domains.coin.domain.CoinTransaction
import com.sclass.domain.domains.coin.domain.CoinTransactionType
import com.sclass.domain.domains.coin.exception.InsufficientCoinException
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class CoinDomainServiceTest {
    private lateinit var coinAdaptor: CoinAdaptor
    private lateinit var coinDomainService: CoinDomainService

    @BeforeEach
    fun setUp() {
        coinAdaptor = mockk(relaxed = true)
        coinDomainService = CoinDomainService(coinAdaptor)
    }

    @Nested
    inner class Issue {
        @Test
        fun `새 Lot 을 생성하고 ISSUED 트랜잭션을 저장한다`() {
            val lotSlot = slot<CoinLot>()
            val txSlot = slot<CoinTransaction>()
            every { coinAdaptor.saveLot(capture(lotSlot)) } answers { lotSlot.captured }
            every { coinAdaptor.saveTransaction(capture(txSlot)) } answers { txSlot.captured }
            every { coinAdaptor.sumActiveLots(any(), any()) } returns 100

            coinDomainService.issue(userId = "user-01", amount = 100, referenceId = "payment-01")

            assertAll(
                { assertEquals(100, lotSlot.captured.amount) },
                { assertEquals(100, lotSlot.captured.remaining) },
                { assertEquals(CoinLotSourceType.PURCHASE, lotSlot.captured.sourceType) },
                { assertEquals(CoinTransactionType.ISSUED, txSlot.captured.type) },
                { assertEquals("payment-01", txSlot.captured.referenceId) },
                { assertEquals(lotSlot.captured.id, txSlot.captured.lotId) },
            )
        }
    }

    @Nested
    inner class Deduct {
        @Test
        fun `만료 임박 Lot 부터 FIFO 로 차감한다`() {
            val now = LocalDateTime.now()
            val lot1 =
                CoinLot(
                    userId = "user-01",
                    amount = 30,
                    remaining = 30,
                    expireAt = now.plusDays(3),
                    sourceType = CoinLotSourceType.PURCHASE,
                )
            val lot2 =
                CoinLot(
                    userId = "user-01",
                    amount = 50,
                    remaining = 50,
                    expireAt = now.plusDays(10),
                    sourceType = CoinLotSourceType.PURCHASE,
                )
            every { coinAdaptor.findActiveLotsForUpdate("user-01", any()) } returns listOf(lot1, lot2)
            val savedLots = slot<List<CoinLot>>()
            val txs = slot<List<CoinTransaction>>()
            every { coinAdaptor.saveLots(capture(savedLots)) } answers { savedLots.captured }
            every { coinAdaptor.saveTransactions(capture(txs)) } answers { txs.captured }
            every { coinAdaptor.sumActiveLots(any(), any()) } returns 40

            coinDomainService.deduct("user-01", 40)

            assertAll(
                { assertEquals(0, lot1.remaining) },
                { assertEquals(40, lot2.remaining) },
                { assertEquals(2, txs.captured.size) },
                { assertEquals(30, txs.captured[0].amount) },
                { assertEquals(10, txs.captured[1].amount) },
                { txs.captured.forEach { assertEquals(CoinTransactionType.DEDUCTED, it.type) } },
            )
        }

        @Test
        fun `총 잔액이 부족하면 InsufficientCoinException 을 던진다`() {
            val lot =
                CoinLot(
                    userId = "user-01",
                    amount = 20,
                    remaining = 20,
                    sourceType = CoinLotSourceType.PURCHASE,
                )
            every { coinAdaptor.findActiveLotsForUpdate(any(), any()) } returns listOf(lot)

            assertThrows(InsufficientCoinException::class.java) {
                coinDomainService.deduct("user-01", 50)
            }
        }

        @Test
        fun `한 Lot 으로 충분하면 그 Lot 만 차감하고 다음 Lot 은 건드리지 않는다`() {
            val now = LocalDateTime.now()
            val lot1 =
                CoinLot(
                    userId = "user-01",
                    amount = 50,
                    remaining = 50,
                    expireAt = now.plusDays(3),
                    sourceType = CoinLotSourceType.PURCHASE,
                )
            val lot2 =
                CoinLot(
                    userId = "user-01",
                    amount = 30,
                    remaining = 30,
                    expireAt = now.plusDays(10),
                    sourceType = CoinLotSourceType.PURCHASE,
                )
            every { coinAdaptor.findActiveLotsForUpdate("user-01", any()) } returns listOf(lot1, lot2)
            val txs = slot<List<CoinTransaction>>()
            every { coinAdaptor.saveLots(any()) } answers { firstArg() }
            every { coinAdaptor.saveTransactions(capture(txs)) } answers { txs.captured }
            every { coinAdaptor.sumActiveLots(any(), any()) } returns 60

            coinDomainService.deduct("user-01", 20)

            assertAll(
                { assertEquals(30, lot1.remaining) },
                { assertEquals(30, lot2.remaining) },
                { assertEquals(1, txs.captured.size) },
                { assertEquals(20, txs.captured[0].amount) },
                { assertEquals(lot1.id, txs.captured[0].lotId) },
            )
        }
    }

    @Nested
    inner class Refund {
        @Test
        fun `환불은 새 REFUND Lot 을 생성한다`() {
            val lotSlot = slot<CoinLot>()
            val txSlot = slot<CoinTransaction>()
            every { coinAdaptor.saveLot(capture(lotSlot)) } answers { lotSlot.captured }
            every { coinAdaptor.saveTransaction(capture(txSlot)) } answers { txSlot.captured }
            every { coinAdaptor.sumActiveLots(any(), any()) } returns 30

            coinDomainService.refund(userId = "user-01", amount = 30, referenceId = "payment-01")

            assertAll(
                { assertEquals(CoinLotSourceType.REFUND, lotSlot.captured.sourceType) },
                { assertEquals(30, lotSlot.captured.remaining) },
                { assertEquals(CoinTransactionType.REFUNDED, txSlot.captured.type) },
                { assertEquals("payment-01", txSlot.captured.referenceId) },
            )
        }
    }
}
