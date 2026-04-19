package com.sclass.domain.domains.coin.service

import com.sclass.domain.config.ConcurrencyTest
import com.sclass.domain.domains.coin.domain.CoinLot
import com.sclass.domain.domains.coin.domain.CoinLotSourceType
import com.sclass.domain.domains.coin.exception.InsufficientCoinException
import com.sclass.domain.domains.coin.repository.CoinLotRepository
import com.sclass.domain.domains.coin.repository.CoinTransactionRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@ConcurrencyTest
class CoinDomainServiceConcurrencyTest {
    @Autowired
    private lateinit var coinDomainService: CoinDomainService

    @Autowired
    private lateinit var coinLotRepository: CoinLotRepository

    @Autowired
    private lateinit var coinTransactionRepository: CoinTransactionRepository

    @Autowired
    private lateinit var txManager: PlatformTransactionManager

    private lateinit var txTemplate: TransactionTemplate

    companion object {
        private const val USER_ID = "test-user-concurrent-01"
    }

    @BeforeEach
    fun setUp() {
        txTemplate = TransactionTemplate(txManager)
        txTemplate.execute {
            coinTransactionRepository.deleteAllInBatch()
            coinLotRepository.deleteAllInBatch()
        }
        txTemplate.execute {
            coinLotRepository.saveAndFlush(
                CoinLot(
                    userId = USER_ID,
                    amount = 100,
                    remaining = 100,
                    sourceType = CoinLotSourceType.PURCHASE,
                ),
            )
        }
    }

    @Test
    fun `동시에 10개 스레드가 각각 50코인을 차감하면 정확히 2번만 성공하고 잔액이 0이 된다`() {
        val threadCount = 10
        val deductAmount = 50
        val readyLatch = CountDownLatch(threadCount)
        val startLatch = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(threadCount)
        val successes = ConcurrentLinkedQueue<String>()
        val failures = ConcurrentLinkedQueue<Exception>()

        repeat(threadCount) { i ->
            executor.submit {
                readyLatch.countDown()
                startLatch.await()
                try {
                    txTemplate.execute {
                        coinDomainService.deduct(
                            userId = USER_ID,
                            amount = deductAmount,
                            referenceId = "thread-$i",
                            description = "동시성 테스트 스레드 $i",
                        )
                    }
                    successes.add("thread-$i")
                } catch (e: Exception) {
                    failures.add(e)
                }
            }
        }

        readyLatch.await()
        startLatch.countDown()
        executor.shutdown()
        executor.awaitTermination(30, java.util.concurrent.TimeUnit.SECONDS)

        val finalBalance =
            txTemplate.execute {
                coinLotRepository.sumActive(USER_ID, LocalDateTime.now())
            }!!

        assertThat(successes.size).isEqualTo(2)
        assertThat(failures.size).isEqualTo(threadCount - 2)
        assertThat(finalBalance).isEqualTo(0)
        failures.forEach { ex ->
            assertThat(rootCause(ex)).isInstanceOf(InsufficientCoinException::class.java)
        }
    }

    @Test
    fun `여러 Lot 이 있을 때 동시 차감은 FIFO 순서로 소진되고 최종 잔액이 정확하다`() {
        val fifoUser = "test-user-fifo-01"
        val now = LocalDateTime.now()
        txTemplate.execute {
            coinLotRepository.saveAndFlush(
                CoinLot(
                    userId = fifoUser,
                    amount = 50,
                    remaining = 50,
                    expireAt = now.plusDays(3),
                    sourceType = CoinLotSourceType.PURCHASE,
                ),
            )
            coinLotRepository.saveAndFlush(
                CoinLot(
                    userId = fifoUser,
                    amount = 50,
                    remaining = 50,
                    expireAt = now.plusDays(30),
                    sourceType = CoinLotSourceType.PURCHASE,
                ),
            )
        }

        val threadCount = 10
        val deductAmount = 30
        val readyLatch = CountDownLatch(threadCount)
        val startLatch = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(threadCount)
        val successes = ConcurrentLinkedQueue<String>()
        val failures = ConcurrentLinkedQueue<Exception>()

        repeat(threadCount) { i ->
            executor.submit {
                readyLatch.countDown()
                startLatch.await()
                try {
                    txTemplate.execute {
                        coinDomainService.deduct(
                            userId = fifoUser,
                            amount = deductAmount,
                            referenceId = "fifo-thread-$i",
                        )
                    }
                    successes.add("fifo-thread-$i")
                } catch (e: Exception) {
                    failures.add(e)
                }
            }
        }

        readyLatch.await()
        startLatch.countDown()
        executor.shutdown()
        executor.awaitTermination(30, java.util.concurrent.TimeUnit.SECONDS)

        val lots =
            txTemplate.execute {
                coinLotRepository
                    .findAll()
                    .filter { it.userId == fifoUser }
                    .sortedBy { it.expireAt }
            }!!
        val finalBalance = lots.sumOf { it.remaining }

        assertThat(successes.size).isEqualTo(3)
        assertThat(failures.size).isEqualTo(threadCount - 3)
        assertThat(finalBalance).isEqualTo(10)
        assertThat(lots[0].remaining).isEqualTo(0)
        assertThat(lots[1].remaining).isEqualTo(10)
        failures.forEach { ex ->
            assertThat(rootCause(ex)).isInstanceOf(InsufficientCoinException::class.java)
        }
    }

    private fun rootCause(t: Throwable): Throwable {
        var cur: Throwable = t
        while (cur.cause != null && cur.cause !== cur) cur = cur.cause!!
        return cur
    }

    @Test
    fun `동시에 같은 유저에게 코인을 발급하면 잔액이 정확해야 한다`() {
        val threadCount = 10
        val issueAmount = 100
        val readyLatch = CountDownLatch(threadCount)
        val startLatch = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(threadCount)
        val successes = ConcurrentLinkedQueue<String>()
        val failures = ConcurrentLinkedQueue<Exception>()

        repeat(threadCount) { i ->
            executor.submit {
                readyLatch.countDown()
                startLatch.await()
                try {
                    txTemplate.execute {
                        coinDomainService.issue(
                            userId = USER_ID,
                            amount = issueAmount,
                            referenceId = "issue-thread-$i",
                        )
                    }
                    successes.add("thread-$i")
                } catch (e: Exception) {
                    failures.add(e)
                }
            }
        }

        readyLatch.await()
        startLatch.countDown()
        executor.shutdown()
        executor.awaitTermination(30, java.util.concurrent.TimeUnit.SECONDS)

        val finalBalance =
            txTemplate.execute {
                coinLotRepository.sumActive(USER_ID, LocalDateTime.now())
            }!!

        val expectedBalance = 100 + (successes.size * issueAmount)
        assertThat(finalBalance).isEqualTo(expectedBalance)
    }
}
