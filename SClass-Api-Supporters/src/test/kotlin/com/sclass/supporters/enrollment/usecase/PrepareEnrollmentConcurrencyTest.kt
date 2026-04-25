package com.sclass.supporters.enrollment.usecase

import com.sclass.domain.domains.course.domain.Course
import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.domain.domains.course.exception.CourseNotEnrollableException
import com.sclass.domain.domains.course.repository.CourseRepository
import com.sclass.domain.domains.enrollment.repository.EnrollmentRepository
import com.sclass.domain.domains.payment.domain.PgType
import com.sclass.domain.domains.payment.repository.PaymentRepository
import com.sclass.domain.domains.product.domain.CourseProduct
import com.sclass.domain.domains.product.repository.ProductRepository
import com.sclass.supporters.config.ApiIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@ApiIntegrationTest
@Testcontainers
class PrepareEnrollmentConcurrencyTest {
    companion object {
        @Container
        @JvmStatic
        val redis: GenericContainer<*> =
            GenericContainer("redis:7-alpine").withExposedPorts(6379)

        @DynamicPropertySource
        @JvmStatic
        fun redisProps(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.redis.host") { redis.host }
            registry.add("spring.data.redis.port") { redis.firstMappedPort }
        }
    }

    @Autowired
    private lateinit var prepareEnrollmentUseCase: PrepareEnrollmentUseCase

    @Autowired
    private lateinit var courseRepository: CourseRepository

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var enrollmentRepository: EnrollmentRepository

    @Autowired
    private lateinit var paymentRepository: PaymentRepository

    @Test
    fun `정원 3명 코스에 10명이 동시 등록 요청하면 3명만 성공한다`() {
        val product =
            productRepository.save(
                CourseProduct(
                    name = "테스트 코스",
                    priceWon = 10_000,
                    totalLessons = 10,
                ),
            )
        val course =
            courseRepository.save(
                Course(
                    productId = product.id,
                    teacherUserId = "teacher-01",
                    status = CourseStatus.LISTED,
                    maxEnrollments = 3,
                ),
            )

        val threadCount = 10
        val readyLatch = CountDownLatch(threadCount)
        val startLatch = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(threadCount)
        val successes = ConcurrentLinkedQueue<Int>()
        val enrollableFailures = ConcurrentLinkedQueue<Int>()
        val otherFailures = ConcurrentLinkedQueue<Exception>()

        try {
            repeat(threadCount) { i ->
                executor.submit {
                    readyLatch.countDown()
                    startLatch.await()
                    try {
                        prepareEnrollmentUseCase.execute(
                            studentUserId = "student-$i",
                            productId = product.id,
                            courseId = course.id,
                            pgType = PgType.NICEPAY,
                        )
                        successes.add(i)
                    } catch (e: CourseNotEnrollableException) {
                        enrollableFailures.add(i)
                    } catch (e: Exception) {
                        otherFailures.add(e)
                    }
                }
            }

            readyLatch.await()
            startLatch.countDown()
            executor.shutdown()
            executor.awaitTermination(60, TimeUnit.SECONDS)

            assertThat(otherFailures)
                .describedAs("예상하지 못한 예외가 발생했습니다: %s", otherFailures)
                .isEmpty()
            assertThat(successes.size)
                .describedAs("정원을 초과해 등록되었습니다")
                .isEqualTo(3)
            assertThat(enrollableFailures.size).isEqualTo(7)

            val liveCount =
                enrollmentRepository.countByCourseIdAndStatusIn(
                    course.id,
                    setOf(
                        com.sclass.domain.domains.enrollment.domain.EnrollmentStatus.PENDING_PAYMENT,
                        com.sclass.domain.domains.enrollment.domain.EnrollmentStatus.ACTIVE,
                    ),
                )
            assertThat(liveCount).isEqualTo(3L)
        } finally {
            enrollmentRepository.deleteAll()
            paymentRepository.deleteAll()
            courseRepository.deleteAll()
            productRepository.deleteAll()
        }
    }
}
