package com.sclass.supporters.enrollment.usecase

import com.sclass.domain.domains.course.adaptor.CourseAdaptor
import com.sclass.domain.domains.course.domain.Course
import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.domain.domains.course.exception.CourseNotActiveException
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.enrollment.domain.Enrollment
import com.sclass.domain.domains.enrollment.domain.EnrollmentStatus
import com.sclass.domain.domains.enrollment.exception.EnrollmentAlreadyExistsException
import com.sclass.domain.domains.payment.adaptor.PaymentAdaptor
import com.sclass.domain.domains.payment.domain.Payment
import com.sclass.domain.domains.payment.domain.PgType
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CoinProduct
import com.sclass.domain.domains.product.domain.CourseProduct
import com.sclass.domain.domains.product.exception.ProductTypeMismatchException
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PrepareEnrollmentUseCaseTest {
    private lateinit var courseAdaptor: CourseAdaptor
    private lateinit var productAdaptor: ProductAdaptor
    private lateinit var enrollmentAdaptor: EnrollmentAdaptor
    private lateinit var paymentAdaptor: PaymentAdaptor
    private lateinit var useCase: PrepareEnrollmentUseCase

    @BeforeEach
    fun setUp() {
        courseAdaptor = mockk()
        productAdaptor = mockk()
        enrollmentAdaptor = mockk()
        paymentAdaptor = mockk()
        useCase = PrepareEnrollmentUseCase(courseAdaptor, productAdaptor, enrollmentAdaptor, paymentAdaptor)
    }

    private fun activeCourse() =
        Course(
            id = 1L,
            productId = "product-id-00000000001",
            teacherUserId = "teacher-id-00000000001",
            name = "2025 수학 코스",
            status = CourseStatus.ACTIVE,
        )

    private fun draftCourse() =
        Course(
            id = 1L,
            productId = "product-id-00000000001",
            teacherUserId = "teacher-id-00000000001",
            name = "2025 수학 코스",
            status = CourseStatus.DRAFT,
        )

    private fun courseProduct() =
        CourseProduct(
            name = "수학 코스",
            priceWon = 300000,
            totalLessons = 12,
        )

    private fun pendingPayment() =
        Payment(
            userId = "student-id-00000000001",
            productId = "product-id-00000000001",
            amount = 300000,
            pgType = PgType.NICEPAY,
            pgOrderId = "order-id-000000000001",
        )

    @Nested
    inner class Success {
        @Test
        fun `결제 준비 시 Payment와 Enrollment가 생성된다`() {
            val enrollmentSlot = slot<Enrollment>()
            every { courseAdaptor.findById(1L) } returns activeCourse()
            every { productAdaptor.findById("product-id-00000000001") } returns courseProduct()
            every { enrollmentAdaptor.findLiveEnrollment(1L, "student-id-00000000001") } returns null
            every { paymentAdaptor.save(any()) } returns pendingPayment()
            every { enrollmentAdaptor.save(capture(enrollmentSlot)) } answers { enrollmentSlot.captured }

            val result = useCase.execute("student-id-00000000001", 1L, PgType.NICEPAY)

            assertThat(result.courseId).isEqualTo(1L)
            assertThat(result.courseName).isEqualTo("2025 수학 코스")
            assertThat(result.amount).isEqualTo(300000)
            assertThat(enrollmentSlot.captured.status).isEqualTo(EnrollmentStatus.PENDING_PAYMENT)
            assertThat(enrollmentSlot.captured.tuitionAmountWon).isEqualTo(300000)
        }
    }

    @Nested
    inner class Failure {
        @Test
        fun `코스가 ACTIVE가 아니면 CourseNotActiveException이 발생한다`() {
            every { courseAdaptor.findById(1L) } returns draftCourse()

            assertThatThrownBy {
                useCase.execute("student-id-00000000001", 1L, PgType.NICEPAY)
            }.isInstanceOf(CourseNotActiveException::class.java)
        }

        @Test
        fun `이미 등록된 코스이면 EnrollmentAlreadyExistsException이 발생한다`() {
            val existingEnrollment =
                Enrollment.createForPurchase(
                    courseId = 1L,
                    studentUserId = "student-id-00000000001",
                    tuitionAmountWon = 300000,
                    paymentId = "payment-id-000000000001",
                )
            every { courseAdaptor.findById(1L) } returns activeCourse()
            every { productAdaptor.findById(any()) } returns courseProduct()
            every { enrollmentAdaptor.findLiveEnrollment(1L, "student-id-00000000001") } returns existingEnrollment

            assertThatThrownBy {
                useCase.execute("student-id-00000000001", 1L, PgType.NICEPAY)
            }.isInstanceOf(EnrollmentAlreadyExistsException::class.java)
        }

        @Test
        fun `CourseProduct가 아닌 상품이면 ProductTypeMismatchException이 발생한다`() {
            every { courseAdaptor.findById(1L) } returns activeCourse()
            every { productAdaptor.findById(any()) } returns CoinProduct(name = "코인", priceWon = 10000, coinAmount = 100)
            every { enrollmentAdaptor.findLiveEnrollment(any(), any()) } returns null

            assertThatThrownBy {
                useCase.execute("student-id-00000000001", 1L, PgType.NICEPAY)
            }.isInstanceOf(ProductTypeMismatchException::class.java)
        }

        @Test
        fun `결제 준비 시 paymentAdaptor와 enrollmentAdaptor save를 각각 호출한다`() {
            every { courseAdaptor.findById(1L) } returns activeCourse()
            every { productAdaptor.findById(any()) } returns courseProduct()
            every { enrollmentAdaptor.findLiveEnrollment(any(), any()) } returns null
            every { paymentAdaptor.save(any()) } returns pendingPayment()
            every { enrollmentAdaptor.save(any()) } answers { firstArg() }

            useCase.execute("student-id-00000000001", 1L, PgType.NICEPAY)

            verify(exactly = 1) { paymentAdaptor.save(any()) }
            verify(exactly = 1) { enrollmentAdaptor.save(any()) }
        }
    }
}
