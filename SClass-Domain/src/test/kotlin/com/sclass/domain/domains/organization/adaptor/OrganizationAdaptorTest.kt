package com.sclass.domain.domains.organization.adaptor

import com.sclass.domain.domains.organization.domain.Organization
import com.sclass.domain.domains.organization.exception.OrganizationNotFoundException
import com.sclass.domain.domains.organization.repository.OrganizationRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Optional

class OrganizationAdaptorTest {
    private lateinit var organizationRepository: OrganizationRepository
    private lateinit var organizationAdaptor: OrganizationAdaptor

    @BeforeEach
    fun setUp() {
        organizationRepository = mockk()
        organizationAdaptor = OrganizationAdaptor(organizationRepository)
    }

    @Nested
    inner class FindById {
        @Test
        fun `존재하는 id로 조회하면 조직을 반환한다`() {
            val organization = mockk<Organization>()
            every { organizationRepository.findById(1L) } returns Optional.of(organization)

            val result = organizationAdaptor.findById(1L)

            assertEquals(organization, result)
        }

        @Test
        fun `존재하지 않는 id로 조회하면 OrganizationNotFoundException이 발생한다`() {
            every { organizationRepository.findById(999L) } returns Optional.empty()

            assertThrows<OrganizationNotFoundException> {
                organizationAdaptor.findById(999L)
            }
        }
    }

    @Nested
    inner class FindByIdOrNull {
        @Test
        fun `존재하지 않는 id로 조회하면 null을 반환한다`() {
            every { organizationRepository.findById(999L) } returns Optional.empty()

            val result = organizationAdaptor.findByIdOrNull(999L)

            assertNull(result)
        }
    }

    @Nested
    inner class FindByDomain {
        @Test
        fun `존재하는 도메인으로 조회하면 조직을 반환한다`() {
            val organization = mockk<Organization>()
            every { organizationRepository.findByDomain("example.com") } returns organization

            val result = organizationAdaptor.findByDomain("example.com")

            assertEquals(organization, result)
        }

        @Test
        fun `존재하지 않는 도메인으로 조회하면 OrganizationNotFoundException이 발생한다`() {
            every { organizationRepository.findByDomain("unknown.com") } returns null

            assertThrows<OrganizationNotFoundException> {
                organizationAdaptor.findByDomain("unknown.com")
            }
        }
    }

    @Nested
    inner class FindByDomainOrNull {
        @Test
        fun `존재하지 않는 도메인으로 조회하면 null을 반환한다`() {
            every { organizationRepository.findByDomain("unknown.com") } returns null

            val result = organizationAdaptor.findByDomainOrNull("unknown.com")

            assertNull(result)
        }
    }

    @Nested
    inner class FindByInviteCode {
        @Test
        fun `존재하는 초대코드로 조회하면 조직을 반환한다`() {
            val organization = mockk<Organization>()
            every { organizationRepository.findByInviteCode("ABC123") } returns organization

            val result = organizationAdaptor.findByInviteCode("ABC123")

            assertEquals(organization, result)
        }

        @Test
        fun `존재하지 않는 초대코드로 조회하면 OrganizationNotFoundException이 발생한다`() {
            every { organizationRepository.findByInviteCode("UNKNOWN") } returns null

            assertThrows<OrganizationNotFoundException> {
                organizationAdaptor.findByInviteCode("UNKNOWN")
            }
        }
    }

    @Nested
    inner class FindByInviteCodeOrNull {
        @Test
        fun `존재하지 않는 초대코드로 조회하면 null을 반환한다`() {
            every { organizationRepository.findByInviteCode("UNKNOWN") } returns null

            val result = organizationAdaptor.findByInviteCodeOrNull("UNKNOWN")

            assertNull(result)
        }
    }

    @Nested
    inner class ExistsByInviteCode {
        @Test
        fun `존재하는 초대코드이면 true를 반환한다`() {
            every { organizationRepository.existsByInviteCode("ABC123") } returns true

            val result = organizationAdaptor.existsByInviteCode("ABC123")

            assertTrue(result)
        }
    }

    @Nested
    inner class Save {
        @Test
        fun `조직 저장을 repository에 위임한다`() {
            val organization = mockk<Organization>()
            every { organizationRepository.save(organization) } returns organization

            val result = organizationAdaptor.save(organization)

            assertEquals(organization, result)
            verify { organizationRepository.save(organization) }
        }
    }
}
