package com.sclass.backoffice.commissionpolicy.usecase

import com.sclass.domain.domains.commission.adaptor.CommissionPolicyAdaptor
import com.sclass.domain.domains.commission.domain.CommissionPolicy
import com.sclass.domain.domains.commission.exception.CommissionPolicyNotFoundException
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UpdateCommissionPolicyActiveUseCaseTest {
    private val commissionPolicyAdaptor = mockk<CommissionPolicyAdaptor>()
    private val useCase = UpdateCommissionPolicyActiveUseCase(commissionPolicyAdaptor)

    private fun policy(active: Boolean) =
        CommissionPolicy(
            name = "기본 의뢰 정책",
            coinCost = 100,
            active = active,
        )

    @Nested
    inner class Execute {
        @Test
        fun `active=false 로 호출하면 정책을 비활성화한다`() {
            val target = policy(active = true)
            every { commissionPolicyAdaptor.findById(target.id) } returns target

            useCase.execute(target.id, active = false)

            assertFalse(target.active)
        }

        @Test
        fun `active=true 로 호출하면 비활성 정책을 다시 활성화한다`() {
            val target = policy(active = false)
            every { commissionPolicyAdaptor.findById(target.id) } returns target

            useCase.execute(target.id, active = true)

            assertTrue(target.active)
        }

        @Test
        fun `존재하지 않는 정책이면 CommissionPolicyNotFoundException 을 던진다`() {
            every { commissionPolicyAdaptor.findById(any()) } throws CommissionPolicyNotFoundException()

            assertThrows(CommissionPolicyNotFoundException::class.java) {
                useCase.execute("non-existent-id", active = false)
            }
        }
    }
}
