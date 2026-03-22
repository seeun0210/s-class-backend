package com.sclass.domain.domains.organization.service

import com.sclass.domain.domains.organization.adaptor.OrganizationAdaptor
import com.sclass.domain.domains.organization.adaptor.OrganizationUserAdaptor
import com.sclass.domain.domains.organization.domain.Organization
import com.sclass.domain.domains.organization.dto.OrganizationUserInfo
import com.sclass.domain.domains.organization.exception.OrganizationNotFoundException
import com.sclass.domain.domains.user.domain.Role
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.LocalDateTime

class OrganizationUserDomainServiceTest {
    private lateinit var organizationAdaptor: OrganizationAdaptor
    private lateinit var organizationUserAdaptor: OrganizationUserAdaptor
    private lateinit var organizationUserDomainService: OrganizationUserDomainService

    @BeforeEach
    fun setUp() {
        organizationAdaptor = mockk()
        organizationUserAdaptor = mockk()
        organizationUserDomainService = OrganizationUserDomainService(organizationAdaptor, organizationUserAdaptor)
    }

    @Nested
    inner class GetUsersByRole {
        @Test
        fun `존재하는 Organization의 역할별 유저 목록을 반환한다`() {
            val organization = mockk<Organization>()
            val pageable = PageRequest.of(0, 20)
            val userInfo =
                OrganizationUserInfo(
                    userId = "user-id",
                    name = "테스트",
                    email = "test@example.com",
                    profileImageUrl = null,
                    role = Role.TEACHER,
                    createdAt = LocalDateTime.now(),
                )
            val page = PageImpl(listOf(userInfo), pageable, 1L)

            every { organizationAdaptor.findById(1L) } returns organization
            every {
                organizationUserAdaptor.findUsersByOrganizationIdAndRole(1L, Role.TEACHER, pageable)
            } returns page

            val result = organizationUserDomainService.getUsersByRole(1L, Role.TEACHER, pageable)

            assertEquals(1, result.totalElements)
            assertEquals("user-id", result.content[0].userId)
            verify { organizationAdaptor.findById(1L) }
        }

        @Test
        fun `존재하지 않는 Organization이면 OrganizationNotFoundException이 발생한다`() {
            val pageable = PageRequest.of(0, 20)

            every { organizationAdaptor.findById(999L) } throws OrganizationNotFoundException()

            assertThrows<OrganizationNotFoundException> {
                organizationUserDomainService.getUsersByRole(999L, Role.TEACHER, pageable)
            }
        }
    }

    @Nested
    inner class GetUserStats {
        @Test
        fun `존재하는 Organization의 역할별 통계를 반환한다`() {
            val organization = mockk<Organization>()
            val roleCounts = mapOf(Role.ADMIN to 2L, Role.TEACHER to 5L, Role.STUDENT to 30L)

            every { organizationAdaptor.findById(1L) } returns organization
            every { organizationUserAdaptor.countByOrganizationIdGroupByRole(1L) } returns roleCounts

            val result = organizationUserDomainService.getUserStats(1L)

            assertEquals(2L, result[Role.ADMIN])
            assertEquals(5L, result[Role.TEACHER])
            assertEquals(30L, result[Role.STUDENT])
            verify { organizationAdaptor.findById(1L) }
        }

        @Test
        fun `존재하지 않는 Organization이면 OrganizationNotFoundException이 발생한다`() {
            every { organizationAdaptor.findById(999L) } throws OrganizationNotFoundException()

            assertThrows<OrganizationNotFoundException> {
                organizationUserDomainService.getUserStats(999L)
            }
        }
    }
}
