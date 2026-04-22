package com.sclass.backoffice.enrollment.usecase

import com.sclass.backoffice.enrollment.dto.ExtendMembershipExpireRequest
import com.sclass.domain.domains.coin.adaptor.CoinAdaptor
import com.sclass.domain.domains.coin.domain.CoinLot
import com.sclass.domain.domains.coin.domain.CoinLotSourceType
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.enrollment.domain.Enrollment
import com.sclass.domain.domains.enrollment.exception.EnrollmentTypeMismatchException
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CourseProduct
import com.sclass.domain.domains.product.domain.RollingMembershipProduct
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class ExtendMembershipExpireUseCaseTest {
    private lateinit var enrollmentAdaptor: EnrollmentAdaptor
    private lateinit var coinAdaptor: CoinAdaptor
    private lateinit var productAdaptor: ProductAdaptor
    private lateinit var useCase: ExtendMembershipExpireUseCase

    @BeforeEach
    fun setUp() {
        enrollmentAdaptor = mockk()
        coinAdaptor = mockk(relaxed = true)
        productAdaptor = mockk()
        useCase = ExtendMembershipExpireUseCase(enrollmentAdaptor, coinAdaptor, productAdaptor)
    }

    @Test
    fun `멤버십 enrollment의 endAt과 귀속 CoinLot의 expireAt을 연장한다`() {
        val newEndAt = LocalDateTime.of(2026, 12, 31, 23, 59)
        val enrollment =
            Enrollment.createMembershipByGrant(
                productId = "product-id-000000000000000000001",
                studentUserId = "student-id-0000000000000000001",
                grantedByUserId = "admin-id-000000000000000000001",
                grantReason = "테스트",
                startAt = LocalDateTime.of(2026, 1, 1, 0, 0),
                endAt = LocalDateTime.of(2026, 3, 31, 23, 59),
            )
        val lot =
            CoinLot(
                userId = "student-id-0000000000000000001",
                amount = 300,
                remaining = 300,
                enrollmentId = 1L,
                expireAt = LocalDateTime.of(2026, 3, 31, 23, 59),
                sourceType = CoinLotSourceType.ADMIN_GRANT,
            )
        every { enrollmentAdaptor.findById(1L) } returns enrollment
        every { productAdaptor.findById("product-id-000000000000000000001") } returns
            RollingMembershipProduct(
                name = "프리미엄 멤버십",
                priceWon = 50000,
                periodDays = 30,
                coinPackageId = "coin-package-id-000000001",
            )
        every { coinAdaptor.findLotsByEnrollmentId(1L) } returns listOf(lot)
        val savedSlot = slot<Enrollment>()
        every { enrollmentAdaptor.save(capture(savedSlot)) } answers { savedSlot.captured }

        useCase.execute(1L, ExtendMembershipExpireRequest(newEndAt))

        assertEquals(newEndAt, savedSlot.captured.endAt)
        assertEquals(newEndAt, lot.expireAt)
        verify { coinAdaptor.saveLots(listOf(lot)) }
    }

    @Test
    fun `코스 enrollment이면 예외가 발생한다`() {
        val enrollment = mockk<Enrollment>()
        every { enrollment.productId } returns "course-product-id-000000000001"
        every { enrollmentAdaptor.findById(2L) } returns enrollment
        every { productAdaptor.findById("course-product-id-000000000001") } returns
            CourseProduct(
                name = "수학 코스",
                priceWon = 300000,
                totalLessons = 12,
            )

        assertThrows<EnrollmentTypeMismatchException> {
            useCase.execute(2L, ExtendMembershipExpireRequest(LocalDateTime.of(2026, 12, 31, 23, 59)))
        }
    }
}
