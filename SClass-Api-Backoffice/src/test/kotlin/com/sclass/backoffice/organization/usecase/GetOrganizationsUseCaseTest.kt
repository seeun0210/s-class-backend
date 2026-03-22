package com.sclass.backoffice.organization.usecase

import com.sclass.domain.domains.organization.adaptor.OrganizationAdaptor
import com.sclass.domain.domains.organization.domain.Organization
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

class GetOrganizationsUseCaseTest {
    private lateinit var organizationAdaptor: OrganizationAdaptor
    private lateinit var getOrganizationsUseCase: GetOrganizationsUseCase

    @BeforeEach
    fun setUp() {
        organizationAdaptor = mockk()
        getOrganizationsUseCase = GetOrganizationsUseCase(organizationAdaptor)
    }

    @Test
    fun `Organization 목록을 페이지네이션으로 조회한다`() {
        val pageable = PageRequest.of(0, 20)
        val organizations =
            listOf(
                Organization(name = "테스트학원", domain = "test.sclass.com"),
                Organization(name = "테스트학원2", domain = "test2.sclass.com"),
            )
        val page = PageImpl(organizations, pageable, 2)
        every { organizationAdaptor.findAll(pageable) } returns page

        val result = getOrganizationsUseCase.execute(pageable)

        assertEquals(2, result.content.size)
        assertEquals(0, result.page)
        assertEquals(20, result.size)
        assertEquals(2, result.totalElements)
        assertEquals(1, result.totalPages)
        assertEquals("테스트학원", result.content[0].name)
    }

    @Test
    fun `빈 목록이면 빈 페이지를 반환한다`() {
        val pageable = PageRequest.of(0, 20)
        val page = PageImpl<Organization>(emptyList(), pageable, 0)
        every { organizationAdaptor.findAll(pageable) } returns page

        val result = getOrganizationsUseCase.execute(pageable)

        assertEquals(0, result.content.size)
        assertEquals(0, result.totalElements)
    }
}
