package com.sclass.supporters.payment.usecase

import com.sclass.domain.domains.coin.adaptor.CoinPackageAdaptor
import com.sclass.domain.domains.coin.domain.CoinPackage
import com.sclass.domain.domains.coin.exception.CoinPackageNotFoundException
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.enrollment.exception.NoActiveMembershipException
import com.sclass.domain.domains.payment.adaptor.PaymentAdaptor
import com.sclass.domain.domains.payment.domain.Payment
import com.sclass.domain.domains.payment.domain.PaymentTargetType
import com.sclass.domain.domains.payment.domain.PgType
import com.sclass.supporters.payment.dto.PreparePaymentRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PreparePaymentUseCaseTest {
    private lateinit var coinPackageAdaptor: CoinPackageAdaptor
    private lateinit var enrollmentAdaptor: EnrollmentAdaptor
    private lateinit var paymentAdaptor: PaymentAdaptor
    private lateinit var useCase: PreparePaymentUseCase

    private val userId = "user-id-00000000000000000000000001"

    @BeforeEach
    fun setUp() {
        coinPackageAdaptor = mockk()
        enrollmentAdaptor = mockk()
        paymentAdaptor = mockk()
        useCase = PreparePaymentUseCase(coinPackageAdaptor, enrollmentAdaptor, paymentAdaptor)
    }

    @Test
    fun `결제 준비 성공 시 paymentId와 pgOrderId를 반환한다`() {
        val coinPackage = CoinPackage(name = "코인 100", priceWon = 1000, coinAmount = 100)
        every { enrollmentAdaptor.hasActiveMembershipEnrollment(userId) } returns true
        every { coinPackageAdaptor.findById(any()) } returns coinPackage

        val paymentSlot = slot<Payment>()
        every { paymentAdaptor.save(capture(paymentSlot)) } answers { paymentSlot.captured }

        val result =
            useCase.execute(
                userId = userId,
                request = PreparePaymentRequest(coinPackageId = coinPackage.id, pgType = PgType.NICEPAY),
            )

        assertAll(
            { assertEquals(coinPackage.priceWon, result.amount) },
            { assertEquals(coinPackage.name, result.productName) },
            { assertEquals(paymentSlot.captured.pgOrderId, result.pgOrderId) },
            { assertEquals(PaymentTargetType.COIN_PACKAGE, paymentSlot.captured.targetType) },
            { assertEquals(coinPackage.id, paymentSlot.captured.targetId) },
        )
    }

    @Test
    fun `활성 멤버십이 없으면 NoActiveMembershipException이 발생한다`() {
        every { enrollmentAdaptor.hasActiveMembershipEnrollment(userId) } returns false

        assertThrows<NoActiveMembershipException> {
            useCase.execute(
                userId = userId,
                request = PreparePaymentRequest(coinPackageId = "cp-id", pgType = PgType.NICEPAY),
            )
        }
    }

    @Test
    fun `존재하지 않는 코인 패키지면 예외가 발생한다`() {
        every { enrollmentAdaptor.hasActiveMembershipEnrollment(userId) } returns true
        every { coinPackageAdaptor.findById(any()) } throws CoinPackageNotFoundException()

        assertThrows<CoinPackageNotFoundException> {
            useCase.execute(
                userId = userId,
                request = PreparePaymentRequest(coinPackageId = "invalid-id", pgType = PgType.NICEPAY),
            )
        }
    }
}
