package com.sclass.backoffice.enrollment.usecase

import com.sclass.backoffice.enrollment.dto.GrantMembershipEnrollmentRequest
import com.sclass.domain.domains.coin.adaptor.CoinPackageAdaptor
import com.sclass.domain.domains.coin.domain.CoinLotSourceType
import com.sclass.domain.domains.coin.domain.CoinPackage
import com.sclass.domain.domains.coin.domain.CoinPackageStatus
import com.sclass.domain.domains.coin.service.CoinDomainService
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.enrollment.domain.Enrollment
import com.sclass.domain.domains.enrollment.domain.EnrollmentStatus
import com.sclass.domain.domains.enrollment.domain.EnrollmentType
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.RollingMembershipProduct
import com.sclass.domain.domains.product.exception.ProductTypeMismatchException
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class GrantMembershipEnrollmentUseCaseTest {
    private lateinit var productAdaptor: ProductAdaptor
    private lateinit var coinPackageAdaptor: CoinPackageAdaptor
    private lateinit var enrollmentAdaptor: EnrollmentAdaptor
    private lateinit var coinDomainService: CoinDomainService
    private lateinit var useCase: GrantMembershipEnrollmentUseCase

    @BeforeEach
    fun setUp() {
        productAdaptor = mockk()
        coinPackageAdaptor = mockk()
        enrollmentAdaptor = mockk()
        coinDomainService = mockk(relaxed = true)
        useCase = GrantMembershipEnrollmentUseCase(productAdaptor, coinPackageAdaptor, enrollmentAdaptor, coinDomainService)
    }

    private fun mockProduct(periodDays: Int = 30): RollingMembershipProduct {
        val product =
            RollingMembershipProduct(
                name = "30일 멤버십",
                priceWon = 99000,
                coinPackageId = "coin-pkg-id-00000000000000000001",
                periodDays = periodDays,
            )
        every { productAdaptor.findById("product-id-000000000000000000001") } returns product
        return product
    }

    private fun mockCoinPackage(): CoinPackage {
        val pkg =
            CoinPackage(
                name = "기본 코인 패키지",
                coinAmount = 300,
                priceWon = 0,
                status = CoinPackageStatus.ACTIVE,
            )
        every { coinPackageAdaptor.findById("coin-pkg-id-00000000000000000001") } returns pkg
        return pkg
    }

    @Test
    fun `멤버십 enrollment를 생성하고 코인을 지급한다`() {
        mockProduct()
        mockCoinPackage()
        val slot = slot<Enrollment>()
        every { enrollmentAdaptor.save(capture(slot)) } answers { slot.captured }

        val result =
            useCase.execute(
                adminUserId = "admin-id-000000000000000000001",
                request =
                    GrantMembershipEnrollmentRequest(
                        studentUserId = "student-id-0000000000000000001",
                        membershipProductId = "product-id-000000000000000000001",
                        grantReason = "테스트 지급",
                    ),
            )

        assertAll(
            { assertEquals(EnrollmentType.ADMIN_GRANT, result.enrollmentType) },
            { assertEquals(EnrollmentStatus.ACTIVE, result.status) },
            { assertEquals("student-id-0000000000000000001", result.studentUserId) },
            { assertEquals(0, result.tuitionAmountWon) },
        )
        verify {
            coinDomainService.issue(
                userId = "student-id-0000000000000000001",
                amount = 300,
                referenceId = any(),
                description = any(),
                enrollmentId = any(),
                expireAt = any(),
                sourceType = CoinLotSourceType.ADMIN_GRANT,
                sourceMeta = any(),
            )
        }
    }

    @Test
    fun `startAt, endAt을 직접 지정하면 product 기본 period를 무시한다`() {
        mockProduct()
        mockCoinPackage()
        val slot = slot<Enrollment>()
        every { enrollmentAdaptor.save(capture(slot)) } answers { slot.captured }

        val customStart = LocalDateTime.of(2026, 5, 1, 0, 0)
        val customEnd = LocalDateTime.of(2026, 7, 31, 23, 59)

        useCase.execute(
            adminUserId = "admin-id-000000000000000000001",
            request =
                GrantMembershipEnrollmentRequest(
                    studentUserId = "student-id-0000000000000000001",
                    membershipProductId = "product-id-000000000000000000001",
                    grantReason = "연장 지급",
                    startAt = customStart,
                    endAt = customEnd,
                ),
        )

        assertEquals(customStart, slot.captured.startAt)
        assertEquals(customEnd, slot.captured.endAt)
    }

    @Test
    fun `MembershipProduct가 아니면 예외가 발생한다`() {
        every { productAdaptor.findById(any()) } returns mockk()

        assertThrows<ProductTypeMismatchException> {
            useCase.execute(
                adminUserId = "admin-id-000000000000000000001",
                request =
                    GrantMembershipEnrollmentRequest(
                        studentUserId = "student-id-0000000000000000001",
                        membershipProductId = "wrong-product-id-000000000000001",
                        grantReason = "테스트",
                    ),
            )
        }
    }
}
