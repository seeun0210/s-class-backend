package com.sclass.backoffice.organization.usecase

import com.sclass.backoffice.organization.dto.CreateOrganizationRequest
import com.sclass.domain.domains.organization.domain.Organization
import com.sclass.domain.domains.organization.service.OrganizationDomainService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

class CreateOrganizationUseCaseTest {
    private lateinit var organizationDomainService: OrganizationDomainService
    private lateinit var createOrganizationUseCase: CreateOrganizationUseCase

    @BeforeEach
    fun setUp() {
        organizationDomainService = mockk()
        createOrganizationUseCase = CreateOrganizationUseCase(organizationDomainService)
    }

    @Test
    fun `Organization을 생성한다`() {
        val request =
            CreateOrganizationRequest(
                name = "테스트학원",
                domain = "test.sclass.com",
                logoUrl = "https://logo.png",
            )
        val organization =
            Organization(
                id = 1L,
                name = "테스트학원",
                domain = "test.sclass.com",
                logoUrl = "https://logo.png",
                inviteCode = "ABC123",
            )
        every {
            organizationDomainService.create(
                name = "테스트학원",
                domain = "test.sclass.com",
                logoUrl = "https://logo.png",
            )
        } returns organization

        val result = createOrganizationUseCase.execute(request)

        assertAll(
            { assertEquals(1L, result.id) },
            { assertEquals("테스트학원", result.name) },
            { assertEquals("test.sclass.com", result.domain) },
            { assertEquals("https://logo.png", result.logoUrl) },
            { assertEquals("ABC123", result.inviteCode) },
        )
    }

    @Test
    fun `logoUrl 없이 Organization을 생성한다`() {
        val request =
            CreateOrganizationRequest(
                name = "테스트학원",
                domain = "test.sclass.com",
            )
        val organization =
            Organization(
                id = 1L,
                name = "테스트학원",
                domain = "test.sclass.com",
            )
        every {
            organizationDomainService.create(
                name = "테스트학원",
                domain = "test.sclass.com",
                logoUrl = null,
            )
        } returns organization

        val result = createOrganizationUseCase.execute(request)

        assertEquals("테스트학원", result.name)
        assertNull(result.logoUrl)
    }
}
