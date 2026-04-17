package com.sclass.backoffice.commissionpolicy.usecase

import com.sclass.domain.domains.commission.adaptor.CommissionPolicyAdaptor
import com.sclass.domain.domains.commission.domain.CommissionPolicy
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GetAdminCommissionPolicyListUseCaseTest {
    private val commissionPolicyAdaptor = mockk<CommissionPolicyAdaptor>()
    private val useCase = GetAdminCommissionPolicyListUseCase(commissionPolicyAdaptor)

    @Nested
    inner class Execute {
        @Test
        fun `활성, 비활성 정책을 모두 포함해 반환한다`() {
            val active = CommissionPolicy(name = "현재 정책", coinCost = 100, active = true)
            val inactive = CommissionPolicy(name = "이전 정책", coinCost = 80, active = false)
            every { commissionPolicyAdaptor.findAll() } returns listOf(active, inactive)

            val result = useCase.execute()

            assertAll(
                { assertEquals(2, result.commissionPolicies.size) },
                { assertTrue(result.commissionPolicies.any { it.id == active.id && it.active }) },
                { assertTrue(result.commissionPolicies.any { it.id == inactive.id && !it.active }) },
            )
        }

        @Test
        fun `정책이 없으면 빈 목록을 반환한다`() {
            every { commissionPolicyAdaptor.findAll() } returns emptyList()

            val result = useCase.execute()

            assertEquals(0, result.commissionPolicies.size)
        }
    }
}
