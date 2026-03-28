package com.sclass.domain.domains.teacherassignment.service

import com.sclass.domain.domains.teacherassignment.adaptor.TeacherAssignmentAdaptor
import com.sclass.domain.domains.teacherassignment.domain.TeacherAssignment
import com.sclass.domain.domains.teacherassignment.exception.InvalidPlatformForAssignmentException
import com.sclass.domain.domains.teacherassignment.exception.OrganizationNotAllowedForSupportersException
import com.sclass.domain.domains.teacherassignment.exception.OrganizationRequiredForLmsException
import com.sclass.domain.domains.teacherassignment.exception.TeacherAssignmentNotFoundException
import com.sclass.domain.domains.user.domain.Platform
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows

class TeacherAssignmentDomainServiceTest {
    private lateinit var teacherAssignmentAdaptor: TeacherAssignmentAdaptor
    private lateinit var teacherAssignmentDomainService: TeacherAssignmentDomainService

    @BeforeEach
    fun setUp() {
        teacherAssignmentAdaptor = mockk()
        teacherAssignmentDomainService = TeacherAssignmentDomainService(teacherAssignmentAdaptor)
    }

    @Nested
    inner class Assign {
        @Test
        fun `LMS 플랫폼에 담당 선생님을 배정한다`() {
            val slot = slot<TeacherAssignment>()
            every {
                teacherAssignmentAdaptor.findActiveByStudentIdAndPlatformAndOrganizationIdOrNull(
                    "student-1",
                    Platform.LMS,
                    1L,
                )
            } returns null
            every { teacherAssignmentAdaptor.save(capture(slot)) } answers { slot.captured }

            val result =
                teacherAssignmentDomainService.assign(
                    studentId = "student-1",
                    teacherId = "teacher-1",
                    platform = Platform.LMS,
                    organizationId = 1L,
                    assignedBy = "admin-1",
                )

            assertAll(
                { assertEquals("student-1", result.studentId) },
                { assertEquals("teacher-1", result.teacherId) },
                { assertEquals(Platform.LMS, result.platform) },
                { assertEquals(1L, result.organizationId) },
                { assertEquals("admin-1", result.assignedBy) },
                { assertNull(result.unassignedAt) },
            )
        }

        @Test
        fun `SUPPORTERS 플랫폼에 담당 선생님을 배정한다`() {
            val slot = slot<TeacherAssignment>()
            every {
                teacherAssignmentAdaptor.findActiveByStudentIdAndPlatformAndOrganizationIdOrNull(
                    "student-1",
                    Platform.SUPPORTERS,
                    null,
                )
            } returns null
            every { teacherAssignmentAdaptor.save(capture(slot)) } answers { slot.captured }

            val result =
                teacherAssignmentDomainService.assign(
                    studentId = "student-1",
                    teacherId = "teacher-1",
                    platform = Platform.SUPPORTERS,
                    organizationId = null,
                    assignedBy = "admin-1",
                )

            assertAll(
                { assertEquals(Platform.SUPPORTERS, result.platform) },
                { assertNull(result.organizationId) },
            )
        }

        @Test
        fun `기존 배정이 있으면 해제하고 새로 배정한다`() {
            val existing =
                TeacherAssignment(
                    studentId = "student-1",
                    teacherId = "old-teacher",
                    platform = Platform.LMS,
                    organizationId = 1L,
                    assignedBy = "admin-1",
                )
            val slot = slot<TeacherAssignment>()
            every {
                teacherAssignmentAdaptor.findActiveByStudentIdAndPlatformAndOrganizationIdOrNull(
                    "student-1",
                    Platform.LMS,
                    1L,
                )
            } returns existing
            every { teacherAssignmentAdaptor.save(capture(slot)) } answers { slot.captured }

            val result =
                teacherAssignmentDomainService.assign(
                    studentId = "student-1",
                    teacherId = "new-teacher",
                    platform = Platform.LMS,
                    organizationId = 1L,
                    assignedBy = "admin-1",
                )

            assertAll(
                { assertNotNull(existing.unassignedAt) },
                { assertEquals("new-teacher", result.teacherId) },
            )
            verify(exactly = 2) { teacherAssignmentAdaptor.save(any()) }
        }

        @Test
        fun `LMS인데 organizationId가 null이면 예외가 발생한다`() {
            assertThrows<OrganizationRequiredForLmsException> {
                teacherAssignmentDomainService.assign(
                    studentId = "student-1",
                    teacherId = "teacher-1",
                    platform = Platform.LMS,
                    organizationId = null,
                    assignedBy = "admin-1",
                )
            }
        }

        @Test
        fun `SUPPORTERS인데 organizationId가 있으면 예외가 발생한다`() {
            assertThrows<OrganizationNotAllowedForSupportersException> {
                teacherAssignmentDomainService.assign(
                    studentId = "student-1",
                    teacherId = "teacher-1",
                    platform = Platform.SUPPORTERS,
                    organizationId = 1L,
                    assignedBy = "admin-1",
                )
            }
        }

        @Test
        fun `BACKOFFICE 플랫폼이면 예외가 발생한다`() {
            assertThrows<InvalidPlatformForAssignmentException> {
                teacherAssignmentDomainService.assign(
                    studentId = "student-1",
                    teacherId = "teacher-1",
                    platform = Platform.BACKOFFICE,
                    organizationId = null,
                    assignedBy = "admin-1",
                )
            }
        }
    }

    @Nested
    inner class Unassign {
        @Test
        fun `현재 배정을 해제한다`() {
            val existing =
                TeacherAssignment(
                    studentId = "student-1",
                    teacherId = "teacher-1",
                    platform = Platform.LMS,
                    organizationId = 1L,
                    assignedBy = "admin-1",
                )
            every {
                teacherAssignmentAdaptor.findActiveByStudentIdAndPlatformAndOrganizationId(
                    "student-1",
                    Platform.LMS,
                    1L,
                )
            } returns existing
            every { teacherAssignmentAdaptor.save(existing) } returns existing

            teacherAssignmentDomainService.unassign(
                studentId = "student-1",
                platform = Platform.LMS,
                organizationId = 1L,
            )

            assertNotNull(existing.unassignedAt)
            verify { teacherAssignmentAdaptor.save(existing) }
        }

        @Test
        fun `활성 배정이 없으면 예외가 발생한다`() {
            every {
                teacherAssignmentAdaptor.findActiveByStudentIdAndPlatformAndOrganizationId(
                    "student-1",
                    Platform.LMS,
                    1L,
                )
            } throws TeacherAssignmentNotFoundException()

            assertThrows<TeacherAssignmentNotFoundException> {
                teacherAssignmentDomainService.unassign(
                    studentId = "student-1",
                    platform = Platform.LMS,
                    organizationId = 1L,
                )
            }
        }

        @Test
        fun `LMS인데 organizationId가 null이면 예외가 발생한다`() {
            assertThrows<OrganizationRequiredForLmsException> {
                teacherAssignmentDomainService.unassign(
                    studentId = "student-1",
                    platform = Platform.LMS,
                    organizationId = null,
                )
            }
        }
    }
}
