package com.sclass.backoffice.organization.usecase

import com.sclass.backoffice.organization.dto.UpdateOrganizationSettingsRequest
import com.sclass.domain.domains.organization.domain.Organization
import com.sclass.domain.domains.organization.domain.OrganizationSettings
import com.sclass.domain.domains.organization.service.OrganizationDomainService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

class UpdateOrganizationSettingsUseCaseTest {
    private lateinit var organizationDomainService: OrganizationDomainService
    private lateinit var updateOrganizationSettingsUseCase: UpdateOrganizationSettingsUseCase

    @BeforeEach
    fun setUp() {
        organizationDomainService = mockk()
        updateOrganizationSettingsUseCase = UpdateOrganizationSettingsUseCase(organizationDomainService)
    }

    @Test
    fun `Organization settings를 수정한다`() {
        val request = UpdateOrganizationSettingsRequest(useSupporters = true, useLms = true)
        val organization =
            Organization(
                id = 1L,
                name = "테스트학원",
                domain = "test.sclass.com",
                settings = OrganizationSettings(useSupporters = true, useLms = true),
            )
        every {
            organizationDomainService.updateSettings(
                id = 1L,
                settings = OrganizationSettings(useSupporters = true, useLms = true),
            )
        } returns organization

        val result = updateOrganizationSettingsUseCase.execute(1L, request)

        assertAll(
            { assertEquals(1L, result.id) },
            { assertTrue(result.useSupporters) },
            { assertTrue(result.useLms) },
        )
    }

    @Test
    fun `Settings를 모두 비활성화한다`() {
        val request = UpdateOrganizationSettingsRequest(useSupporters = false, useLms = false)
        val organization =
            Organization(
                id = 1L,
                name = "테스트학원",
                domain = "test.sclass.com",
                settings = OrganizationSettings(useSupporters = false, useLms = false),
            )
        every {
            organizationDomainService.updateSettings(
                id = 1L,
                settings = OrganizationSettings(useSupporters = false, useLms = false),
            )
        } returns organization

        val result = updateOrganizationSettingsUseCase.execute(1L, request)

        assertAll(
            { assertFalse(result.useSupporters) },
            { assertFalse(result.useLms) },
        )
    }
}
