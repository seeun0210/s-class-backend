package com.sclass.backoffice.enrollment.usecase

import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.enrollment.domain.Enrollment
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.ProductType
import com.sclass.domain.domains.product.domain.RollingMembershipProduct
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class GetEnrollmentDetailUseCaseTest {
    private lateinit var enrollmentAdaptor: EnrollmentAdaptor
    private lateinit var productAdaptor: ProductAdaptor
    private lateinit var useCase: GetEnrollmentDetailUseCase

    @BeforeEach
    fun setUp() {
        enrollmentAdaptor = mockk()
        productAdaptor = mockk()
        useCase = GetEnrollmentDetailUseCase(enrollmentAdaptor, productAdaptor)
    }

    @Test
    fun `상품이 존재하면 enrollment 상세에 상품 정보를 포함한다`() {
        val enrollment = createMembershipEnrollment()
        val product =
            RollingMembershipProduct(
                name = "30일 멤버십",
                priceWon = 99000,
                coinPackageId = "coin-package-id-0000000001",
                periodDays = 30,
            )

        every { enrollmentAdaptor.findById(1L) } returns enrollment
        every { productAdaptor.findByIdOrNull("product-id-000000000000000001") } returns product

        val result = useCase.execute(1L)

        assertAll(
            { assertEquals("product-id-000000000000000001", result.productId) },
            { assertEquals("30일 멤버십", result.productName) },
            { assertEquals(ProductType.ROLLING_MEMBERSHIP, result.productType) },
        )
    }

    @Test
    fun `상품이 없어도 enrollment 상세를 반환한다`() {
        val enrollment = createMembershipEnrollment()

        every { enrollmentAdaptor.findById(1L) } returns enrollment
        every { productAdaptor.findByIdOrNull("product-id-000000000000000001") } returns null

        val result = useCase.execute(1L)

        assertAll(
            { assertEquals("product-id-000000000000000001", result.productId) },
            { assertNull(result.productName) },
            { assertNull(result.productType) },
        )
    }

    private fun createMembershipEnrollment(): Enrollment =
        Enrollment.createMembershipByGrant(
            productId = "product-id-000000000000000001",
            studentUserId = "student-id-000000000000000001",
            grantedByUserId = "admin-id-00000000000000000001",
            grantReason = "테스트 지급",
            startAt = LocalDateTime.of(2026, 1, 1, 0, 0),
            endAt = LocalDateTime.of(2026, 1, 31, 23, 59),
        )
}
