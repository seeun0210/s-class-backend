package com.sclass.domain.domains.organization.service

import com.sclass.domain.domains.organization.adaptor.OrganizationAdaptor
import com.sclass.domain.domains.organization.domain.Organization
import com.sclass.domain.domains.organization.domain.OrganizationSettings
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OrganizationDomainServiceTest {
    private lateinit var organizationAdaptor: OrganizationAdaptor
    private lateinit var organizationDomainService: OrganizationDomainService

    @BeforeEach
    fun setUp() {
        organizationAdaptor = mockk()
        organizationDomainService = OrganizationDomainService(organizationAdaptor)
    }

    @Nested
    inner class Create {
        @Test
        fun `기관을 생성하고 초대코드를 발급한다`() {
            val slot = slot<Organization>()
            every { organizationAdaptor.existsByInviteCode(any()) } returns false
            every { organizationAdaptor.save(capture(slot)) } answers { slot.captured }

            val result = organizationDomainService.create(name = "테스트학원", domain = "test.sclass.com")

            assertEquals("테스트학원", result.name)
            assertEquals("test.sclass.com", result.domain)
            assertNotNull(result.inviteCode)
            assertEquals(6, result.inviteCode!!.length)
        }

        @Test
        fun `중복된 초대코드가 있으면 새 코드를 생성한다`() {
            val slot = slot<Organization>()
            every { organizationAdaptor.existsByInviteCode(any()) } returns true andThen false
            every { organizationAdaptor.save(capture(slot)) } answers { slot.captured }

            val result = organizationDomainService.create(name = "테스트학원", domain = "test.sclass.com")

            assertNotNull(result.inviteCode)
            verify(atLeast = 2) { organizationAdaptor.existsByInviteCode(any()) }
        }
    }

    @Nested
    inner class UpdateSettings {
        @Test
        fun `기관 설정을 업데이트한다`() {
            val organization = mockk<Organization>(relaxed = true)
            val newSettings = OrganizationSettings(useSupporters = true)
            every { organizationAdaptor.findById(1L) } returns organization
            every { organizationAdaptor.save(organization) } returns organization

            val result = organizationDomainService.updateSettings(1L, newSettings)

            assertEquals(organization, result)
            verify { organization.changeSettings(newSettings) }
            verify { organizationAdaptor.save(organization) }
        }

        @Test
        fun `LMS 기능을 활성화한다`() {
            val organization = mockk<Organization>(relaxed = true)
            val newSettings = OrganizationSettings(useLms = true)
            every { organizationAdaptor.findById(1L) } returns organization
            every { organizationAdaptor.save(organization) } returns organization

            val result = organizationDomainService.updateSettings(1L, newSettings)

            assertEquals(organization, result)
            verify { organization.changeSettings(newSettings) }
        }

        @Test
        fun `Supporters와 LMS를 모두 활성화한다`() {
            val organization = mockk<Organization>(relaxed = true)
            val newSettings = OrganizationSettings(useSupporters = true, useLms = true)
            every { organizationAdaptor.findById(1L) } returns organization
            every { organizationAdaptor.save(organization) } returns organization

            val result = organizationDomainService.updateSettings(1L, newSettings)

            assertEquals(organization, result)
            verify { organization.changeSettings(newSettings) }
        }
    }

    @Nested
    inner class UpdateLogoUrl {
        @Test
        fun `로고 URL을 업데이트한다`() {
            val organization = mockk<Organization>(relaxed = true)
            every { organizationAdaptor.findById(1L) } returns organization
            every { organizationAdaptor.save(organization) } returns organization

            val result = organizationDomainService.updateLogoUrl(1L, "https://logo.png")

            assertEquals(organization, result)
            verify { organization.changeLogoUrl("https://logo.png") }
        }
    }

    @Nested
    inner class FindByDomain {
        @Test
        fun `서브도메인으로 기관을 조회한다`() {
            val organization = mockk<Organization>()
            every { organizationAdaptor.findByDomain("test.sclass.com") } returns organization

            val result = organizationDomainService.findByDomain("test.sclass.com")

            assertEquals(organization, result)
        }
    }

    @Nested
    inner class FindByInviteCode {
        @Test
        fun `초대코드로 기관을 조회한다`() {
            val organization = mockk<Organization>()
            every { organizationAdaptor.findByInviteCode("ABC123") } returns organization

            val result = organizationDomainService.findByInviteCode("ABC123")

            assertEquals(organization, result)
        }
    }
}
