package com.sclass.backoffice.organization.usecase

import com.sclass.domain.domains.organization.service.OrganizationUserDomainService
import com.sclass.domain.domains.user.domain.Role
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GetOrganizationStatsUseCaseTest {
    private lateinit var organizationUserDomainService: OrganizationUserDomainService
    private lateinit var useCase: GetOrganizationStatsUseCase

    @BeforeEach
    fun setUp() {
        organizationUserDomainService = mockk()
        useCase = GetOrganizationStatsUseCase(organizationUserDomainService)
    }

    @Nested
    inner class Execute {
        @Test
        fun `역할별 통계를 조회하면 각 역할의 카운트와 합계를 반환한다`() {
            val roleCounts = mapOf(Role.ADMIN to 2L, Role.TEACHER to 5L, Role.STUDENT to 30L)

            every { organizationUserDomainService.getUserStats(1L) } returns roleCounts

            val result = useCase.execute(1L)

            assertEquals(37L, result.totalCount)
            assertEquals(2L, result.adminCount)
            assertEquals(5L, result.teacherCount)
            assertEquals(30L, result.studentCount)
            verify { organizationUserDomainService.getUserStats(1L) }
        }

        @Test
        fun `일부 역할이 없으면 해당 역할의 카운트는 0이다`() {
            val roleCounts = mapOf(Role.TEACHER to 3L)

            every { organizationUserDomainService.getUserStats(1L) } returns roleCounts

            val result = useCase.execute(1L)

            assertEquals(3L, result.totalCount)
            assertEquals(0L, result.adminCount)
            assertEquals(3L, result.teacherCount)
            assertEquals(0L, result.studentCount)
        }

        @Test
        fun `소속 유저가 없으면 모든 카운트가 0이다`() {
            every { organizationUserDomainService.getUserStats(1L) } returns emptyMap()

            val result = useCase.execute(1L)

            assertEquals(0L, result.totalCount)
            assertEquals(0L, result.adminCount)
            assertEquals(0L, result.teacherCount)
            assertEquals(0L, result.studentCount)
        }
    }
}
