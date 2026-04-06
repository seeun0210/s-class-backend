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

/**
 * CoinBalance 동시성 테스트.
 *
 * @DataJpaTest는 테스트 메서드를 하나의 트랜잭션으로 감싸므로,
 * 멀티스레드 동시성 검증 시 각 스레드가 독립 트랜잭션을 사용하도록
 * TransactionTemplate을 직접 사용한다.
 */
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

        // @DataJpaTest 트랜잭션 밖에서 데이터 준비
        txTemplate.execute {
            coinTransactionRepository.deleteAll()
            coinBalanceRepository.deleteAll()
            coinBalanceRepository.saveAndFlush(
                CoinBalance(userId = USER_ID, balance = 100, totalIssued = 100),
            )
        }
    }

    @Test
    fun `동시에 10개 스레드가 각각 50코인을 차감하면 최대 2번만 성공해야 한다`() {
        val threadCount = 10
        val deductAmount = 50
        val readyLatch = CountDownLatch(threadCount) // 모든 스레드 준비 대기
        val startLatch = CountDownLatch(1) // 동시 출발 신호
        val executor = Executors.newFixedThreadPool(threadCount)
        val successes = ConcurrentLinkedQueue<String>()
        val failures = ConcurrentLinkedQueue<Exception>()

        repeat(threadCount) { i ->
            executor.submit {
                readyLatch.countDown()
                startLatch.await() // 모든 스레드가 동시에 출발

                try {
                    // 각 스레드가 독립 트랜잭션에서 실행
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

        readyLatch.await() // 모든 스레드가 준비될 때까지 대기
        startLatch.countDown() // 동시 출발!

        executor.shutdown()
        executor.awaitTermination(30, java.util.concurrent.TimeUnit.SECONDS)

        // 검증: 잔액이 음수가 되면 안 된다
        val finalBalance =
            txTemplate.execute {
                coinBalanceRepository.findByUserId(USER_ID)!!
            }!!

        println("=== 동시성 테스트 결과 ===")
        println("성공: ${successes.size}건, 실패: ${failures.size}건")
        println("최종 잔액: ${finalBalance.balance}")
        println("실패 예외 종류: ${failures.map { it.javaClass.simpleName }.distinct()}")

        // 핵심 검증: 잔액이 0 이상이어야 한다 (음수 = 동시성 버그)
        assertThat(finalBalance.balance).isGreaterThanOrEqualTo(0)

        // 성공 횟수는 최대 2번 (100 / 50 = 2)
        assertThat(successes.size).isLessThanOrEqualTo(2)

        // 전체 = 성공 + 실패
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

        println("=== 코인 발급 동시성 테스트 결과 ===")
        println("성공: ${successes.size}건, 실패: ${failures.size}건")
        println("최종 잔액: ${finalBalance.balance}")

        // @Version이 있으므로 일부는 OptimisticLockException으로 실패할 수 있음
        // 성공한 만큼만 잔액이 증가해야 함
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

        println("=== 신규 유저 동시 발급 테스트 ===")
        println("성공: ${successes.size}건, 실패: ${failures.size}건")
        println("실패 예외: ${failures.map { it.javaClass.simpleName }.distinct()}")

        // CoinBalance 레코드는 정확히 1개여야 한다
        val balances =
            txTemplate.execute {
                coinBalanceRepository.findByUserId(newUserId)
            }
        assertThat(balances).isNotNull
    }
}
