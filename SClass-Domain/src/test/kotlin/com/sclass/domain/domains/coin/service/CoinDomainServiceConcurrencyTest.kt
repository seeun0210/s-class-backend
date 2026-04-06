package com.sclass.domain.domains.coin.service

import com.sclass.domain.config.ConcurrencyTest
import com.sclass.domain.domains.coin.domain.CoinBalance
import com.sclass.domain.domains.coin.repository.CoinBalanceRepository
import com.sclass.domain.domains.coin.repository.CoinTransactionRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@ConcurrencyTest
class CoinDomainServiceConcurrencyTest {
    @Autowired
    private lateinit var coinDomainService: CoinDomainService

    @Autowired
    private lateinit var coinBalanceRepository: CoinBalanceRepository

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
            coinBalanceRepository.deleteAllInBatch()
        }
        txTemplate.execute {
            coinBalanceRepository.saveAndFlush(
                CoinBalance(userId = USER_ID, balance = 100, totalIssued = 100),
            )
        }
    }

    @Test
    fun `동시에 10개 스레드가 각각 50코인을 차감하면 최대 2번만 성공해야 한다`() {
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
                coinBalanceRepository.findByUserId(USER_ID)!!
            }!!

        assertThat(finalBalance.balance).isGreaterThanOrEqualTo(0)
        assertThat(successes.size).isLessThanOrEqualTo(2)
        assertThat(successes.size + failures.size).isEqualTo(threadCount)
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
                coinBalanceRepository.findByUserId(USER_ID)!!
            }!!

        val expectedBalance = 100 + (successes.size * issueAmount)
        assertThat(finalBalance.balance).isEqualTo(expectedBalance)
    }

    @Test
    fun `CoinBalance가 없는 유저에게 동시에 발급하면 하나만 생성되어야 한다`() {
        val newUserId = "new-user-concurrent-01"
        val threadCount = 5
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
                            userId = newUserId,
                            amount = 100,
                            referenceId = "new-user-thread-$i",
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

        val balance =
            txTemplate.execute {
                coinBalanceRepository.findByUserId(newUserId)
            }

        assertThat(balance).isNotNull
        assertThat(successes.size).isEqualTo(1)
        assertThat(failures.size).isEqualTo(threadCount - 1)
        assertThat(balance!!.balance).isEqualTo(100)
        assertThat(balance.totalIssued).isEqualTo(100)
    }
}
