package com.sclass.supporters.enrollment.usecase

import com.sclass.domain.domains.course.adaptor.CourseAdaptor
import com.sclass.domain.domains.course.domain.Course
import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.domain.domains.course.exception.CourseNotEnrollableException
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.enrollment.domain.Enrollment
import com.sclass.domain.domains.enrollment.domain.EnrollmentStatus
import com.sclass.domain.domains.enrollment.exception.EnrollmentAlreadyExistsException
import com.sclass.domain.domains.enrollment.exception.EnrollmentInvalidPurchaseTargetException
import com.sclass.domain.domains.payment.adaptor.PaymentAdaptor
import com.sclass.domain.domains.payment.domain.Payment
import com.sclass.domain.domains.payment.domain.PaymentTargetType
import com.sclass.domain.domains.payment.domain.PgType
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CourseProduct
import com.sclass.domain.domains.product.domain.Product
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

    private fun listedCourse() =
        Course(
            id = 1L,
            productId = "product-id-00000000001",
            teacherUserId = "teacher-id-00000000001",
            status = CourseStatus.LISTED,
        )

    private fun draftCourse() =
        Course(
            id = 1L,
            productId = "product-id-00000000001",
            teacherUserId = "teacher-id-00000000001",
            status = CourseStatus.DRAFT,
        )

    private fun courseProduct() =
        CourseProduct(
            name = "수학 코스",
            priceWon = 300000,
            totalLessons = 12,
        )

    private fun matchingCourseProduct() =
        CourseProduct(
            name = "매칭형 수학 코스",
            priceWon = 300000,
            totalLessons = 12,
            matchingEnabled = true,
        )

    private fun pendingPayment() =
        Payment(
            userId = "student-id-00000000001",
            targetType = PaymentTargetType.COURSE_PRODUCT,
            targetId = "product-id-00000000001",
            amount = 300000,
            pgType = PgType.NICEPAY,
            pgOrderId = "order-id-000000000001",
        )

    @Nested
    inner class Success {
        @Test
        fun `결제 준비 시 Payment와 Enrollment가 생성된다`() {
            val enrollmentSlot = slot<Enrollment>()
            every { courseAdaptor.findById(1L) } returns listedCourse()
            every { enrollmentAdaptor.countLiveEnrollments(1L) } returns 0L
            every { productAdaptor.findById("product-id-00000000001") } returns courseProduct()
            every { enrollmentAdaptor.findResumableEnrollment(1L, "student-id-00000000001") } returns null
            every { paymentAdaptor.save(any()) } returns pendingPayment()
            every { enrollmentAdaptor.save(capture(enrollmentSlot)) } answers { enrollmentSlot.captured }

            val result = useCase.execute("student-id-00000000001", "product-id-00000000001", 1L, PgType.NICEPAY)

            assertThat(result.productId).isEqualTo("product-id-00000000001")
            assertThat(result.courseId).isEqualTo(1L)
            assertThat(result.courseName).isEqualTo("수학 코스")
            assertThat(result.amount).isEqualTo(300000)
            assertThat(enrollmentSlot.captured.productId).isEqualTo("product-id-00000000001")
            assertThat(enrollmentSlot.captured.status).isEqualTo(EnrollmentStatus.PENDING_PAYMENT)
            assertThat(enrollmentSlot.captured.tuitionAmountWon).isEqualTo(300000)
        }

        @Test
        fun `매칭형 상품 결제 준비 시 courseId 없이 Payment와 Enrollment가 생성된다`() {
            val enrollmentSlot = slot<Enrollment>()
            every { productAdaptor.findById("product-id-00000000001") } returns matchingCourseProduct()
            every {
                enrollmentAdaptor.findResumableCourseProductEnrollment(
                    "product-id-00000000001",
                    "student-id-00000000001",
                )
            } returns null
            every { paymentAdaptor.save(any()) } returns pendingPayment()
            every { enrollmentAdaptor.save(capture(enrollmentSlot)) } answers { enrollmentSlot.captured }

            val result = useCase.execute("student-id-00000000001", "product-id-00000000001", null, PgType.NICEPAY)

            assertThat(result.productId).isEqualTo("product-id-00000000001")
            assertThat(result.courseId).isNull()
            assertThat(result.courseName).isEqualTo("매칭형 수학 코스")
            assertThat(enrollmentSlot.captured.courseId).isNull()
            assertThat(enrollmentSlot.captured.productId).isEqualTo("product-id-00000000001")
            verify(exactly = 0) { courseAdaptor.findById(any()) }
        }

        @Test
        fun `매칭형 상품에 PENDING_PAYMENT enrollment이 있으면 기존 결제 정보를 그대로 반환한다`() {
            val existingEnrollment =
                Enrollment.createForPurchase(
                    productId = "product-id-00000000001",
                    courseId = null,
                    studentUserId = "student-id-00000000001",
                    tuitionAmountWon = 300000,
                    paymentId = "payment-id-000000000001",
                )
            every { productAdaptor.findById("product-id-00000000001") } returns matchingCourseProduct()
            every {
                enrollmentAdaptor.findResumableCourseProductEnrollment(
                    "product-id-00000000001",
                    "student-id-00000000001",
                )
            } returns existingEnrollment
            every { paymentAdaptor.findById("payment-id-000000000001") } returns pendingPayment()

            val result = useCase.execute("student-id-00000000001", "product-id-00000000001", null, PgType.NICEPAY)

            assertThat(result.productId).isEqualTo("product-id-00000000001")
            assertThat(result.courseId).isNull()
            assertThat(result.pgOrderId).isEqualTo("order-id-000000000001")
            verify(exactly = 0) { paymentAdaptor.save(any()) }
            verify(exactly = 0) { enrollmentAdaptor.save(any()) }
        }
    }

    @Nested
    inner class Failure {
        @Test
        fun `코스가 등록 불가능하면 CourseNotEnrollableException이 발생한다`() {
            every { courseAdaptor.findById(1L) } returns draftCourse()
            every { productAdaptor.findById(any()) } returns courseProduct()
            every { enrollmentAdaptor.findResumableEnrollment(any(), any()) } returns null
            every { enrollmentAdaptor.countLiveEnrollments(1L) } returns 0L

            assertThatThrownBy {
                useCase.execute("student-id-00000000001", "product-id-00000000001", 1L, PgType.NICEPAY)
            }.isInstanceOf(CourseNotEnrollableException::class.java)
        }

        @Test
        fun `ACTIVE enrollment이 있으면 EnrollmentAlreadyExistsException이 발생한다`() {
            every { courseAdaptor.findById(1L) } returns listedCourse()
            every { productAdaptor.findById(any()) } returns courseProduct()
            every { enrollmentAdaptor.findResumableEnrollment(1L, "student-id-00000000001") } throws EnrollmentAlreadyExistsException()

            assertThatThrownBy {
                useCase.execute("student-id-00000000001", "product-id-00000000001", 1L, PgType.NICEPAY)
            }.isInstanceOf(EnrollmentAlreadyExistsException::class.java)
        }

        @Test
        fun `PENDING_PAYMENT enrollment이 있으면 기존 결제 정보를 그대로 반환한다`() {
            val existingEnrollment =
                Enrollment.createForPurchase(
                    productId = "product-id-00000000001",
                    courseId = 1L,
                    studentUserId = "student-id-00000000001",
                    tuitionAmountWon = 300000,
                    paymentId = "payment-id-000000000001",
                )
            every { courseAdaptor.findById(1L) } returns listedCourse()
            every { productAdaptor.findById(any()) } returns courseProduct()
            every { enrollmentAdaptor.findResumableEnrollment(1L, "student-id-00000000001") } returns existingEnrollment
            every { paymentAdaptor.findById("payment-id-000000000001") } returns pendingPayment()

            val result = useCase.execute("student-id-00000000001", "product-id-00000000001", 1L, PgType.NICEPAY)

            assertThat(result.productId).isEqualTo("product-id-00000000001")
            assertThat(result.pgOrderId).isEqualTo("order-id-000000000001")
            verify(exactly = 0) { paymentAdaptor.save(any()) }
            verify(exactly = 0) { enrollmentAdaptor.save(any()) }
        }

        @Test
        fun `CourseProduct가 아닌 상품이면 ProductTypeMismatchException이 발생한다`() {
            every { courseAdaptor.findById(1L) } returns listedCourse()
            every { enrollmentAdaptor.countLiveEnrollments(1L) } returns 0L
            every { productAdaptor.findById(any()) } returns mockk<Product>()
            every { enrollmentAdaptor.findResumableEnrollment(any(), any()) } returns null

            assertThatThrownBy {
                useCase.execute("student-id-00000000001", "product-id-00000000001", 1L, PgType.NICEPAY)
            }.isInstanceOf(ProductTypeMismatchException::class.java)
        }

        @Test
        fun `courseId와 productId 조합이 다르면 EnrollmentInvalidPurchaseTargetException이 발생한다`() {
            every { courseAdaptor.findById(1L) } returns listedCourse()
            every { productAdaptor.findById("different-product-id") } returns courseProduct()

            assertThatThrownBy {
                useCase.execute("student-id-00000000001", "different-product-id", 1L, PgType.NICEPAY)
            }.isInstanceOf(EnrollmentInvalidPurchaseTargetException::class.java)
        }

        @Test
        fun `매칭형 상품에 courseId를 보내면 EnrollmentInvalidPurchaseTargetException이 발생한다`() {
            every { productAdaptor.findById("product-id-00000000001") } returns matchingCourseProduct()

            assertThatThrownBy {
                useCase.execute("student-id-00000000001", "product-id-00000000001", 1L, PgType.NICEPAY)
            }.isInstanceOf(EnrollmentInvalidPurchaseTargetException::class.java)
        }

        @Test
        fun `매칭형 상품에 PENDING_MATCH enrollment이 있으면 EnrollmentAlreadyExistsException이 발생한다`() {
            every { productAdaptor.findById("product-id-00000000001") } returns matchingCourseProduct()
            every {
                enrollmentAdaptor.findResumableCourseProductEnrollment(
                    "product-id-00000000001",
                    "student-id-00000000001",
                )
            } throws EnrollmentAlreadyExistsException()

            assertThatThrownBy {
                useCase.execute("student-id-00000000001", "product-id-00000000001", null, PgType.NICEPAY)
            }.isInstanceOf(EnrollmentAlreadyExistsException::class.java)
        }

        @Test
        fun `결제 준비 시 paymentAdaptor와 enrollmentAdaptor save를 각각 호출한다`() {
            every { courseAdaptor.findById(1L) } returns listedCourse()
            every { enrollmentAdaptor.countLiveEnrollments(1L) } returns 0L
            every { productAdaptor.findById(any()) } returns courseProduct()
            every { enrollmentAdaptor.findResumableEnrollment(any(), any()) } returns null
            every { paymentAdaptor.save(any()) } returns pendingPayment()
            every { enrollmentAdaptor.save(any()) } answers { firstArg() }

            useCase.execute("student-id-00000000001", "product-id-00000000001", 1L, PgType.NICEPAY)

            verify(exactly = 1) { paymentAdaptor.save(any()) }
            verify(exactly = 1) { enrollmentAdaptor.save(any()) }
        }
    }
}
