package com.sclass.backoffice.organization.usecase

import com.sclass.domain.domains.organization.adaptor.OrganizationAdaptor
import com.sclass.domain.domains.organization.adaptor.OrganizationUserAdaptor
import com.sclass.domain.domains.organization.domain.Organization
import com.sclass.domain.domains.organization.dto.OrganizationUserInfo
import com.sclass.domain.domains.user.domain.Role
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.LocalDateTime

class GetOrganizationUsersUseCaseTest {
    private lateinit var organizationAdaptor: OrganizationAdaptor
    private lateinit var organizationUserAdaptor: OrganizationUserAdaptor
    private lateinit var useCase: GetOrganizationUsersUseCase

    @BeforeEach
    fun setUp() {
        organizationAdaptor = mockk()
        organizationUserAdaptor = mockk()
        useCase = GetOrganizationUsersUseCase(organizationAdaptor, organizationUserAdaptor)
    }

    @Test
    fun `역할별 유저 목록을 조회하면 페이지 응답을 반환한다`() {
        val pageable = PageRequest.of(0, 20)
        val userInfo =
            OrganizationUserInfo(
                userId = "user-id",
                name = "테스트",
                email = "test@example.com",
                profileImageUrl = "https://example.com/image.png",
                role = Role.TEACHER,
                createdAt = LocalDateTime.of(2026, 1, 1, 0, 0),
            )
        val page = PageImpl(listOf(userInfo), pageable, 1L)

        every { organizationAdaptor.findById(1L) } returns mockk<Organization>()
        every {
            organizationUserAdaptor.findUsersByOrganizationIdAndRole(1L, Role.TEACHER, pageable)
        } returns page

        val result = useCase.execute(1L, Role.TEACHER, pageable)

        assertEquals(1, result.content.size)
        assertEquals("user-id", result.content[0].userId)
        assertEquals("테스트", result.content[0].name)
        assertEquals("TEACHER", result.content[0].role)
        assertEquals("https://example.com/image.png", result.content[0].profileImageUrl)
        assertEquals(0, result.page)
        assertEquals(20, result.size)
        assertEquals(1L, result.totalElements)
        assertEquals(1, result.totalPages)
        verify { organizationAdaptor.findById(1L) }
        verify { organizationUserAdaptor.findUsersByOrganizationIdAndRole(1L, Role.TEACHER, pageable) }
    }

    @Test
    fun `유저가 없으면 빈 페이지 응답을 반환한다`() {
        val pageable = PageRequest.of(0, 20)
        val emptyPage = PageImpl<OrganizationUserInfo>(emptyList(), pageable, 0L)

        every { organizationAdaptor.findById(1L) } returns mockk<Organization>()
        every {
            organizationUserAdaptor.findUsersByOrganizationIdAndRole(1L, Role.ADMIN, pageable)
        } returns emptyPage

        val result = useCase.execute(1L, Role.ADMIN, pageable)

        assertEquals(0, result.content.size)
        assertEquals(0L, result.totalElements)
    }
}
