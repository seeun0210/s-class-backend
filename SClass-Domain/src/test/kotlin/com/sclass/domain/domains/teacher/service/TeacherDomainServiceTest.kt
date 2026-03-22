package com.sclass.domain.domains.teacher.service

import com.sclass.domain.domains.teacher.adaptor.TeacherAdaptor
import com.sclass.domain.domains.teacher.domain.Teacher
import com.sclass.domain.domains.teacher.exception.TeacherAlreadyExistsException
import com.sclass.domain.domains.user.domain.User
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TeacherDomainServiceTest {
    private lateinit var teacherAdaptor: TeacherAdaptor
    private lateinit var teacherDomainService: TeacherDomainService

    private lateinit var user: User

    @BeforeEach
    fun setUp() {
        teacherAdaptor = mockk()
        teacherDomainService = TeacherDomainService(teacherAdaptor)
        user = mockk<User>()
        every { user.id } returns "user-id"
    }

    @Nested
    inner class Register {
        @Test
        fun `교사를 기관에 등록한다`() {
            val slot = slot<Teacher>()
            every { teacherAdaptor.existsByUserIdAndOrganizationId("user-id", 1L) } returns false
            every { teacherAdaptor.save(capture(slot)) } answers { slot.captured }

            val result = teacherDomainService.register(user = user, organizationId = 1L)

            assertEquals(user, result.user)
            assertEquals(1L, result.organizationId)
        }

        @Test
        fun `기관 없이 교사를 등록한다`() {
            val slot = slot<Teacher>()
            every { teacherAdaptor.existsByUserIdAndOrganizationIdIsNull("user-id") } returns false
            every { teacherAdaptor.save(capture(slot)) } answers { slot.captured }

            val result = teacherDomainService.register(user = user, organizationId = null)

            assertEquals(user, result.user)
            assertNull(result.organizationId)
        }

        @Test
        fun `이미 해당 기관에 등록된 교사이면 TeacherAlreadyExistsException이 발생한다`() {
            every { teacherAdaptor.existsByUserIdAndOrganizationId("user-id", 1L) } returns true

            assertThrows<TeacherAlreadyExistsException> {
                teacherDomainService.register(user = user, organizationId = 1L)
            }
        }

        @Test
        fun `기관 없이 이미 등록된 교사이면 TeacherAlreadyExistsException이 발생한다`() {
            every { teacherAdaptor.existsByUserIdAndOrganizationIdIsNull("user-id") } returns true

            assertThrows<TeacherAlreadyExistsException> {
                teacherDomainService.register(user = user, organizationId = null)
            }
        }
    }

    @Nested
    inner class FindAllByUserId {
        @Test
        fun `userId로 조회하면 해당 유저의 교사 목록을 반환한다`() {
            val teachers = listOf(mockk<Teacher>(), mockk<Teacher>())
            every { teacherAdaptor.findAllByUserId("user-id") } returns teachers

            val result = teacherDomainService.findAllByUserId("user-id")

            assertEquals(2, result.size)
            assertEquals(teachers, result)
        }
    }

    @Nested
    inner class FindAllByOrganizationId {
        @Test
        fun `기관의 교사 목록을 반환한다`() {
            val teachers = listOf(mockk<Teacher>(), mockk<Teacher>())
            every { teacherAdaptor.findAllByOrganizationId(1L) } returns teachers

            val result = teacherDomainService.findAllByOrganizationId(1L)

            assertEquals(2, result.size)
        }
    }
}
