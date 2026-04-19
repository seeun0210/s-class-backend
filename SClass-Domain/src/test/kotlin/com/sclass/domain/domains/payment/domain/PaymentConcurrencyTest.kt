package com.sclass.domain.domains.payment.domain

import com.sclass.domain.config.ConcurrencyTest
import com.sclass.domain.domains.coin.repository.CoinLotRepository
import com.sclass.domain.domains.coin.repository.CoinTransactionRepository
import com.sclass.domain.domains.coin.service.CoinDomainService
import com.sclass.domain.domains.payment.repository.PaymentRepository
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
import java.util.concurrent.TimeUnit

@ConcurrencyTest
class PaymentConcurrencyTest {
    @Autowired
    private lateinit var paymentRepository: PaymentRepository

    @Autowired
    private lateinit var coinLotRepository: CoinLotRepository

    @Autowired
    private lateinit var coinTransactionRepository: CoinTransactionRepository

    @Autowired
    private lateinit var coinDomainService: CoinDomainService

    @Autowired
    private lateinit var txManager: PlatformTransactionManager

    private lateinit var txTemplate: TransactionTemplate

    companion object {
        private const val USER_ID = "payment-test-user-01"
        private const val PG_ORDER_ID = "ORDER-CONCURRENT-001"
        private const val COIN_AMOUNT = 1000
    }

    @BeforeEach
    fun setUp() {
        txTemplate = TransactionTemplate(txManager)
        txTemplate.execute {
            coinTransactionRepository.deleteAllInBatch()
            coinLotRepository.deleteAllInBatch()
            paymentRepository.deleteAllInBatch()
        }
        txTemplate.execute {
            paymentRepository.saveAndFlush(
                Payment(
                    userId = USER_ID,
                    targetType = PaymentTargetType.COIN_PACKAGE,
                    targetId = "coin-pkg-01",
                    amount = 10000,
                    pgType = PgType.NICEPAY,
                    pgOrderId = PG_ORDER_ID,
                    status = PaymentStatus.PENDING,
                ),
            )
        }
    }

    @Test
    fun `Return과 Webhook이 동시에 같은 PENDING 결제를 처리하면 코인은 1번만 지급되어야 한다`() {
        val threadCount = 2
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
                        val payment = paymentRepository.findByPgOrderId(PG_ORDER_ID)!!
                        if (payment.status != PaymentStatus.PENDING) {
                            throw IllegalStateException("이미 처리된 결제")
                        }
                        payment.markPgApproved("TID-$i")
                        coinDomainService.issue(
                            userId = payment.userId,
                            amount = COIN_AMOUNT,
                            referenceId = payment.id,
                            description = "동시 처리 테스트 - 스레드 $i",
                        )
                        payment.markCompleted()
                        paymentRepository.save(payment)
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
        executor.awaitTermination(30, TimeUnit.SECONDS)

        val finalBalance =
            txTemplate.execute {
                coinLotRepository.sumActive(USER_ID, LocalDateTime.now())
            }!!
        val finalPayment =
            txTemplate.execute {
                paymentRepository.findByPgOrderId(PG_ORDER_ID)!!
            }!!

        assertThat(finalBalance)
            .describedAs("코인이 중복 지급되었습니다!")
            .isEqualTo(COIN_AMOUNT)
        assertThat(successes.size).isEqualTo(1)
        assertThat(finalPayment.status).isEqualTo(PaymentStatus.COMPLETED)
    }

    @Test
    fun `5개 스레드가 동시에 같은 PENDING 결제를 처리하면 코인은 1번만 지급되어야 한다`() {
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
                        val payment = paymentRepository.findByPgOrderId(PG_ORDER_ID)!!
                        if (payment.status != PaymentStatus.PENDING) {
                            throw IllegalStateException("이미 처리된 결제")
                        }
                        payment.markPgApproved("TID-$i")
                        coinDomainService.issue(
                            userId = payment.userId,
                            amount = COIN_AMOUNT,
                            referenceId = payment.id,
                        )
                        payment.markCompleted()
                        paymentRepository.save(payment)
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
        executor.awaitTermination(30, TimeUnit.SECONDS)

        val finalBalance =
            txTemplate.execute {
                coinLotRepository.sumActive(USER_ID, LocalDateTime.now())
            }!!

        assertThat(finalBalance)
            .describedAs("코인이 중복 지급되었습니다!")
            .isLessThanOrEqualTo(COIN_AMOUNT)
    }
}
