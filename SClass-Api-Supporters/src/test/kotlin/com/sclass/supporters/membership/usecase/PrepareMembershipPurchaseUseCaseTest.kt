package com.sclass.supporters.membership.usecase

import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.enrollment.domain.Enrollment
import com.sclass.domain.domains.enrollment.domain.EnrollmentStatus
import com.sclass.domain.domains.enrollment.exception.EnrollmentAlreadyExistsException
import com.sclass.domain.domains.enrollment.exception.MembershipCapacityExceededException
import com.sclass.domain.domains.payment.adaptor.PaymentAdaptor
import com.sclass.domain.domains.payment.domain.Payment
import com.sclass.domain.domains.payment.domain.PaymentTargetType
import com.sclass.domain.domains.payment.domain.PgType
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CohortMembershipProduct
import com.sclass.domain.domains.product.domain.CourseProduct
import com.sclass.domain.domains.product.domain.MembershipProduct
import com.sclass.domain.domains.product.domain.RollingMembershipProduct
import com.sclass.domain.domains.product.exception.CohortSaleEndedException
import com.sclass.domain.domains.product.exception.ProductNotPurchasableException
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
import java.time.LocalDateTime

class PrepareMembershipPurchaseUseCaseTest {
    private lateinit var productAdaptor: ProductAdaptor
    private lateinit var enrollmentAdaptor: EnrollmentAdaptor
    private lateinit var paymentAdaptor: PaymentAdaptor
    private lateinit var useCase: PrepareMembershipPurchaseUseCase

    private val studentUserId = "student-id-00000000001"
    private val membershipProductId = "mp-0000000000000000000000001"

    @BeforeEach
    fun setUp() {
        productAdaptor = mockk()
        enrollmentAdaptor = mockk()
        paymentAdaptor = mockk()
        useCase = PrepareMembershipPurchaseUseCase(productAdaptor, enrollmentAdaptor, paymentAdaptor)
    }

    private fun visibleMembership(maxEnrollments: Int? = null): MembershipProduct {
        val product =
            RollingMembershipProduct(
                name = "프리미엄 멤버십",
                priceWon = 10000,
                periodDays = 30,
                maxEnrollments = maxEnrollments,
                coinPackageId = "cp-0000000000000000000000001",
            )
        product.show()
        return product
    }

    private fun hiddenMembership(): MembershipProduct =
        RollingMembershipProduct(
            name = "비공개 멤버십",
            priceWon = 10000,
            periodDays = 30,
            coinPackageId = "cp-0000000000000000000000001",
        )

    private fun visibleCohortMembership(
        startAt: LocalDateTime,
        endAt: LocalDateTime,
    ): CohortMembershipProduct {
        val product =
            CohortMembershipProduct(
                name = "2026 봄 기수",
                priceWon = 10000,
                coinPackageId = "cp-0000000000000000000000001",
                startAt = startAt,
                endAt = endAt,
            )
        product.show()
        return product
    }

    private fun pendingPayment() =
        Payment(
            userId = studentUserId,
            targetType = PaymentTargetType.MEMBERSHIP_PRODUCT,
            targetId = membershipProductId,
            amount = 10000,
            pgType = PgType.NICEPAY,
            pgOrderId = "order-id-000000000001",
        )

    @Nested
    inner class Success {
        @Test
        fun `멤버십 구매 준비 시 Payment와 Enrollment가 생성된다`() {
            val product = visibleMembership()
            val enrollmentSlot = slot<Enrollment>()
            every { productAdaptor.findById(membershipProductId) } returns product
            every { enrollmentAdaptor.findResumableMembershipEnrollment(product.id, studentUserId) } returns null
            every { paymentAdaptor.save(any()) } returns pendingPayment()
            every { enrollmentAdaptor.save(capture(enrollmentSlot)) } answers { enrollmentSlot.captured }

            val result = useCase.execute(studentUserId, membershipProductId, PgType.NICEPAY)

            assertThat(result.productId).isEqualTo(product.id)
            assertThat(result.productName).isEqualTo("프리미엄 멤버십")
            assertThat(result.amount).isEqualTo(10000)
            assertThat(enrollmentSlot.captured.status).isEqualTo(EnrollmentStatus.PENDING_PAYMENT)
            assertThat(enrollmentSlot.captured.productId).isEqualTo(product.id)
            assertThat(enrollmentSlot.captured.courseId).isNull()
            verify(exactly = 1) { paymentAdaptor.save(any()) }
            verify(exactly = 1) { enrollmentAdaptor.save(any()) }
        }

        @Test
        fun `maxEnrollments null이면 정원 검증을 건너뛴다`() {
            val product = visibleMembership(maxEnrollments = null)
            every { productAdaptor.findById(membershipProductId) } returns product
            every { enrollmentAdaptor.findResumableMembershipEnrollment(product.id, studentUserId) } returns null
            every { paymentAdaptor.save(any()) } returns pendingPayment()
            every { enrollmentAdaptor.save(any()) } answers { firstArg() }

            useCase.execute(studentUserId, membershipProductId, PgType.NICEPAY)

            verify(exactly = 0) { enrollmentAdaptor.countLiveMembershipEnrollments(any()) }
        }

        @Test
        fun `maxEnrollments가 있으면 live 수보다 클 때 통과한다`() {
            val product = visibleMembership(maxEnrollments = 10)
            every { productAdaptor.findById(membershipProductId) } returns product
            every { enrollmentAdaptor.countLiveMembershipEnrollments(product.id) } returns 5L
            every { enrollmentAdaptor.findResumableMembershipEnrollment(product.id, studentUserId) } returns null
            every { paymentAdaptor.save(any()) } returns pendingPayment()
            every { enrollmentAdaptor.save(any()) } answers { firstArg() }

            useCase.execute(studentUserId, membershipProductId, PgType.NICEPAY)

            verify(exactly = 1) { enrollmentAdaptor.countLiveMembershipEnrollments(product.id) }
        }
    }

    @Nested
    inner class Failure {
        @Test
        fun `MembershipProduct가 아니면 ProductTypeMismatchException이 발생한다`() {
            val courseProduct = CourseProduct(name = "수학", priceWon = 10000, totalLessons = 12)
            every { productAdaptor.findById(membershipProductId) } returns courseProduct

            assertThatThrownBy {
                useCase.execute(studentUserId, membershipProductId, PgType.NICEPAY)
            }.isInstanceOf(ProductTypeMismatchException::class.java)
        }

        @Test
        fun `상품이 비공개이면 ProductNotPurchasableException이 발생한다`() {
            every { productAdaptor.findById(membershipProductId) } returns hiddenMembership()

            assertThatThrownBy {
                useCase.execute(studentUserId, membershipProductId, PgType.NICEPAY)
            }.isInstanceOf(ProductNotPurchasableException::class.java)
        }

        @Test
        fun `정원 초과 시 MembershipCapacityExceededException이 발생한다`() {
            val product = visibleMembership(maxEnrollments = 10)
            every { productAdaptor.findById(membershipProductId) } returns product
            every { enrollmentAdaptor.findResumableMembershipEnrollment(product.id, studentUserId) } returns null
            every { enrollmentAdaptor.countLiveMembershipEnrollments(product.id) } returns 10L

            assertThatThrownBy {
                useCase.execute(studentUserId, membershipProductId, PgType.NICEPAY)
            }.isInstanceOf(MembershipCapacityExceededException::class.java)
        }

        @Test
        fun `Cohort 판매 기간이 종료되었으면 CohortSaleEndedException이 발생한다`() {
            val now = LocalDateTime.now()
            val product =
                visibleCohortMembership(
                    startAt = now.minusDays(30),
                    endAt = now.minusDays(1),
                )
            every { productAdaptor.findById(membershipProductId) } returns product

            assertThatThrownBy {
                useCase.execute(studentUserId, membershipProductId, PgType.NICEPAY)
            }.isInstanceOf(CohortSaleEndedException::class.java)
        }

        @Test
        fun `ACTIVE 멤버십이 이미 있으면 EnrollmentAlreadyExistsException이 발생한다`() {
            val product = visibleMembership()
            every { productAdaptor.findById(membershipProductId) } returns product
            every { enrollmentAdaptor.findResumableMembershipEnrollment(product.id, studentUserId) } throws
                EnrollmentAlreadyExistsException()

            assertThatThrownBy {
                useCase.execute(studentUserId, membershipProductId, PgType.NICEPAY)
            }.isInstanceOf(EnrollmentAlreadyExistsException::class.java)
        }

        @Test
        fun `PENDING_PAYMENT 멤버십이 이미 있으면 기존 결제 정보를 반환한다`() {
            val product = visibleMembership()
            val existing =
                Enrollment.createForMembershipPurchase(
                    productId = product.id,
                    studentUserId = studentUserId,
                    tuitionAmountWon = 10000,
                    paymentId = "payment-id-000000000001",
                )
            every { productAdaptor.findById(membershipProductId) } returns product
            every { enrollmentAdaptor.findResumableMembershipEnrollment(product.id, studentUserId) } returns existing
            every { paymentAdaptor.findById("payment-id-000000000001") } returns pendingPayment()

            val result = useCase.execute(studentUserId, membershipProductId, PgType.NICEPAY)

            assertThat(result.pgOrderId).isEqualTo("order-id-000000000001")
            verify(exactly = 0) { paymentAdaptor.save(any()) }
            verify(exactly = 0) { enrollmentAdaptor.save(any()) }
        }
    }
}
