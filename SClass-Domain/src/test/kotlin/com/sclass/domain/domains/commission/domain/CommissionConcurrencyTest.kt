package com.sclass.domain.domains.commission.domain

import com.sclass.domain.config.ConcurrencyTest
import com.sclass.domain.domains.commission.repository.CommissionRepository
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Commission 상태 전이 동시성 테스트.
 *
 * @Version(Optimistic Lock)이 없으면 두 스레드가 동시에 같은 Commission을 읽고
 * 각각 다른 상태로 변경할 때, 나중에 커밋한 쪽이 먼저 커밋한 변경을 덮어쓴다 (Lost Update).
 * @Version이 있으면 먼저 커밋한 쪽만 성공하고, 나중 쪽은 OptimisticLockException으로 실패한다.
 */
@ConcurrencyTest
class CommissionConcurrencyTest {
    @Autowired
    private lateinit var commissionRepository: CommissionRepository

    @Autowired
    private lateinit var entityManager: EntityManager

    @Autowired
    private lateinit var txManager: PlatformTransactionManager

    private lateinit var txTemplate: TransactionTemplate

    @BeforeEach
    fun setUp() {
        txTemplate = TransactionTemplate(txManager)
        txTemplate.execute {
            entityManager.createNativeQuery("DELETE FROM commission_files").executeUpdate()
            entityManager.createNativeQuery("DELETE FROM commission_support_tickets").executeUpdate()
            entityManager.createNativeQuery("DELETE FROM commission_topics").executeUpdate()
            entityManager.createNativeQuery("DELETE FROM commissions").executeUpdate()
        }
    }

    private fun createCommission(status: CommissionStatus = CommissionStatus.REQUESTED): Long {
        val commission =
            txTemplate.execute {
                commissionRepository.saveAndFlush(
                    Commission(
                        studentUserId = "student-user-0000000000001",
                        teacherUserId = "teacher-user-0000000000001",
                        productId = "product-id-00000000000001",
                        outputFormat = OutputFormat.REPORT,
                        activityType = ActivityType.CAREER_EXPLORATION,
                        status = status,
                        guideInfo =
                            GuideInfo(
                                subject = "테스트 과목",
                                volume = "1단원",
                                gradingCriteria = "평가기준",
                                teacherEmphasis = "강조사항",
                            ),
                    ),
                )
            }!!
        return commission.id
    }

    @Test
    fun `동시에 같은 Commission 상태를 변경하면 하나만 성공해야 한다 - Lost Update 방지`() {
        val commissionId = createCommission(CommissionStatus.REQUESTED)

        val threadCount = 2
        val readyLatch = CountDownLatch(threadCount)
        val startLatch = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(threadCount)
        val successes = ConcurrentLinkedQueue<String>()
        val failures = ConcurrentLinkedQueue<Exception>()

        // 스레드 A: REQUESTED → TOPIC_PROPOSED (선생님이 주제 제안)
        executor.submit {
            readyLatch.countDown()
            startLatch.await()
            try {
                txTemplate.execute {
                    val commission = commissionRepository.findById(commissionId).orElseThrow()
                    commission.proposeTopics()
                    commissionRepository.saveAndFlush(commission)
                }
                successes.add("proposeTopics")
            } catch (e: Exception) {
                failures.add(e)
            }
        }

        // 스레드 B: REQUESTED → CANCELLED (학생이 취소)
        executor.submit {
            readyLatch.countDown()
            startLatch.await()
            try {
                txTemplate.execute {
                    val commission = commissionRepository.findById(commissionId).orElseThrow()
                    commission.cancel()
                    commissionRepository.saveAndFlush(commission)
                }
                successes.add("cancel")
            } catch (e: Exception) {
                failures.add(e)
            }
        }

        readyLatch.await()
        startLatch.countDown()
        executor.shutdown()
        executor.awaitTermination(30, TimeUnit.SECONDS)

        val finalCommission =
            txTemplate.execute {
                commissionRepository.findById(commissionId).orElseThrow()
            }!!

        assertThat(successes.size)
            .describedAs("동시 상태 변경 중 하나만 성공해야 합니다 (successes=$successes, failures=${failures.map { it.javaClass.simpleName }})")
            .isEqualTo(1)
        assertThat(failures.size).isEqualTo(1)
        assertThat(finalCommission.status)
            .describedAs("최종 상태는 성공한 하나의 전이 결과여야 합니다")
            .isIn(CommissionStatus.TOPIC_PROPOSED, CommissionStatus.CANCELLED)
    }

    @Test
    fun `5개 스레드가 동시에 같은 Commission 상태를 변경하면 하나만 성공해야 한다`() {
        val commissionId = createCommission(CommissionStatus.REQUESTED)

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
                        val commission = commissionRepository.findById(commissionId).orElseThrow()
                        commission.cancel()
                        commissionRepository.saveAndFlush(commission)
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

        val finalCommission =
            txTemplate.execute {
                commissionRepository.findById(commissionId).orElseThrow()
            }!!

        assertThat(successes.size)
            .describedAs("5개 동시 요청 중 하나만 성공해야 합니다")
            .isEqualTo(1)
        assertThat(finalCommission.status).isEqualTo(CommissionStatus.CANCELLED)
        assertThat(finalCommission.version).isEqualTo(1)
    }
}
