package com.sclass.domain.domains.organization.service

import com.sclass.domain.domains.organization.adaptor.OrganizationAttributionAdaptor
import com.sclass.domain.domains.organization.domain.AttributionSource
import com.sclass.domain.domains.organization.domain.OrganizationAttribution
import com.sclass.domain.domains.organization.exception.OrganizationAlreadyAttributedException
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class OrganizationAttributionDomainServiceTest {
    private lateinit var organizationAttributionAdaptor: OrganizationAttributionAdaptor
    private lateinit var organizationAttributionDomainService: OrganizationAttributionDomainService

    @BeforeEach
    fun setUp() {
        organizationAttributionAdaptor = mockk()
        organizationAttributionDomainService = OrganizationAttributionDomainService(organizationAttributionAdaptor)
    }

    @Nested
    inner class Attribute {
        @Test
        fun `학생을 기관에 귀속시킨다`() {
            val slot = slot<OrganizationAttribution>()
            every { organizationAttributionAdaptor.existsByStudentId("student-id") } returns false
            every { organizationAttributionAdaptor.save(capture(slot)) } answers { slot.captured }

            val result =
                organizationAttributionDomainService.attribute(
                    organizationId = 1L,
                    studentId = "student-id",
                    source = AttributionSource.INVITE_CODE,
                )

            assertEquals(1L, result.organizationId)
            assertEquals("student-id", result.studentId)
            assertEquals(AttributionSource.INVITE_CODE, result.source)
        }

        @Test
        fun `이미 귀속된 학생이면 OrganizationAlreadyAttributedException이 발생한다`() {
            every { organizationAttributionAdaptor.existsByStudentId("student-id") } returns true

            assertThrows<OrganizationAlreadyAttributedException> {
                organizationAttributionDomainService.attribute(
                    organizationId = 1L,
                    studentId = "student-id",
                    source = AttributionSource.INVITE_CODE,
                )
            }
        }
    }

    @Nested
    inner class IsAttributed {
        @Test
        fun `귀속된 학생이면 true를 반환한다`() {
            every { organizationAttributionAdaptor.existsByStudentId("student-id") } returns true

            assertTrue(organizationAttributionDomainService.isAttributed("student-id"))
        }

        @Test
        fun `귀속되지 않은 학생이면 false를 반환한다`() {
            every { organizationAttributionAdaptor.existsByStudentId("unknown-id") } returns false

            assertFalse(organizationAttributionDomainService.isAttributed("unknown-id"))
        }
    }

    @Nested
    inner class FindByStudentId {
        @Test
        fun `학생의 귀속 정보를 반환한다`() {
            val attribution = mockk<OrganizationAttribution>()
            every { organizationAttributionAdaptor.findByStudentId("student-id") } returns attribution

            val result = organizationAttributionDomainService.findByStudentId("student-id")

            assertEquals(attribution, result)
        }
    }

    @Nested
    inner class FindAllByOrganizationId {
        @Test
        fun `기관의 귀속 목록을 반환한다`() {
            val attributions = listOf(mockk<OrganizationAttribution>(), mockk<OrganizationAttribution>())
            every { organizationAttributionAdaptor.findAllByOrganizationId(1L) } returns attributions

            val result = organizationAttributionDomainService.findAllByOrganizationId(1L)

            assertEquals(2, result.size)
        }
    }

    @Nested
    inner class CountByOrganizationId {
        @Test
        fun `기관의 귀속 수를 반환한다`() {
            every { organizationAttributionAdaptor.countByOrganizationId(1L) } returns 5L

            val result = organizationAttributionDomainService.countByOrganizationId(1L)

            assertEquals(5L, result)
        }
    }
}
