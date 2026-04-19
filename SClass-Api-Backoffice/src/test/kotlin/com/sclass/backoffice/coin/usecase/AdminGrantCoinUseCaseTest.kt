package com.sclass.backoffice.coin.usecase

import com.sclass.backoffice.coin.dto.AdminGrantCoinRequest
import com.sclass.domain.domains.coin.domain.CoinLot
import com.sclass.domain.domains.coin.domain.CoinLotSourceType
import com.sclass.domain.domains.coin.service.CoinDomainService
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.enrollment.domain.Enrollment
import com.sclass.domain.domains.enrollment.exception.EnrollmentTypeMismatchException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class AdminGrantCoinUseCaseTest {
    private lateinit var coinDomainService: CoinDomainService
    private lateinit var enrollmentAdaptor: EnrollmentAdaptor
    private lateinit var useCase: AdminGrantCoinUseCase

    @BeforeEach
    fun setUp() {
        coinDomainService = mockk()
        enrollmentAdaptor = mockk()
        useCase = AdminGrantCoinUseCase(coinDomainService, enrollmentAdaptor)
    }

    private fun mockMembershipEnrollment(id: Long): Enrollment =
        mockk<Enrollment>().also {
            every { it.productId } returns "product-id-000000000000000000001"
            every { enrollmentAdaptor.findById(id) } returns it
        }

    private fun mockCourseEnrollment(id: Long): Enrollment =
        mockk<Enrollment>().also {
            every { it.productId } returns null
            every { enrollmentAdaptor.findById(id) } returns it
        }

    @Test
    fun `코인을 지급하고 CoinLot 정보를 반환한다`() {
        val expireAt = LocalDateTime.of(2026, 12, 31, 23, 59)
        val lot =
            CoinLot(
                id = "lot-id-000000000000000000001",
                userId = "student-id-0000000000000000001",
                amount = 100,
                remaining = 100,
                expireAt = expireAt,
                sourceType = CoinLotSourceType.ADMIN_GRANT,
            )
        every {
            coinDomainService.issue(
                userId = any(),
                amount = any(),
                description = any(),
                enrollmentId = null,
                expireAt = any(),
                sourceType = CoinLotSourceType.ADMIN_GRANT,
                sourceMeta = any(),
            )
        } returns lot

        val result =
            useCase.execute(
                adminUserId = "admin-id-000000000000000000001",
                request =
                    AdminGrantCoinRequest(
                        studentUserId = "student-id-0000000000000000001",
                        amount = 100,
                        grantReason = "테스트 보상",
                        expireAt = expireAt,
                    ),
            )

        assertAll(
            { assertEquals("lot-id-000000000000000000001", result.lotId) },
            { assertEquals("student-id-0000000000000000001", result.userId) },
            { assertEquals(100, result.amount) },
            { assertEquals(CoinLotSourceType.ADMIN_GRANT, result.sourceType) },
            { assertEquals(expireAt, result.expireAt) },
        )
        verify {
            coinDomainService.issue(
                userId = "student-id-0000000000000000001",
                amount = 100,
                description = any(),
                enrollmentId = null,
                expireAt = expireAt,
                sourceType = CoinLotSourceType.ADMIN_GRANT,
                sourceMeta = any(),
            )
        }
    }

    @Test
    fun `멤버십 enrollment에 귀속하여 코인을 지급한다`() {
        mockMembershipEnrollment(1L)
        val lot =
            CoinLot(
                id = "lot-id-000000000000000000002",
                userId = "student-id-0000000000000000001",
                amount = 50,
                remaining = 50,
                enrollmentId = 1L,
                sourceType = CoinLotSourceType.ADMIN_GRANT,
            )
        every {
            coinDomainService.issue(
                userId = any(),
                amount = any(),
                description = any(),
                enrollmentId = 1L,
                expireAt = null,
                sourceType = CoinLotSourceType.ADMIN_GRANT,
                sourceMeta = any(),
            )
        } returns lot

        val result =
            useCase.execute(
                adminUserId = "admin-id-000000000000000000001",
                request =
                    AdminGrantCoinRequest(
                        studentUserId = "student-id-0000000000000000001",
                        amount = 50,
                        grantReason = "멤버십 추가 지급",
                        enrollmentId = 1L,
                    ),
            )

        assertEquals(1L, result.enrollmentId)
    }

    @Test
    fun `코스 enrollment에 귀속하려 하면 예외가 발생한다`() {
        mockCourseEnrollment(2L)

        assertThrows<EnrollmentTypeMismatchException> {
            useCase.execute(
                adminUserId = "admin-id-000000000000000000001",
                request =
                    AdminGrantCoinRequest(
                        studentUserId = "student-id-0000000000000000001",
                        amount = 50,
                        grantReason = "잘못된 지급",
                        enrollmentId = 2L,
                    ),
            )
        }
    }

    @Test
    fun `expireAt 없이 지급하면 만료일 없는 CoinLot을 반환한다`() {
        val lot =
            CoinLot(
                id = "lot-id-000000000000000000003",
                userId = "student-id-0000000000000000001",
                amount = 50,
                remaining = 50,
                expireAt = null,
                sourceType = CoinLotSourceType.ADMIN_GRANT,
            )
        every {
            coinDomainService.issue(
                userId = any(),
                amount = any(),
                description = any(),
                enrollmentId = null,
                expireAt = null,
                sourceType = CoinLotSourceType.ADMIN_GRANT,
                sourceMeta = any(),
            )
        } returns lot

        val result =
            useCase.execute(
                adminUserId = "admin-id-000000000000000000001",
                request =
                    AdminGrantCoinRequest(
                        studentUserId = "student-id-0000000000000000001",
                        amount = 50,
                        grantReason = "만료일 없는 지급",
                    ),
            )

        assertEquals(null, result.expireAt)
    }
}
