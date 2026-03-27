package com.sclass.domain.domains.teacher.service

import com.sclass.domain.domains.teacher.adaptor.TeacherAdaptor
import com.sclass.domain.domains.teacher.adaptor.TeacherDocumentAdaptor
import com.sclass.domain.domains.teacher.domain.Teacher
import com.sclass.domain.domains.teacher.exception.TeacherAlreadyExistsException
import com.sclass.domain.domains.user.adaptor.UserRoleAdaptor
import com.sclass.domain.domains.user.domain.User
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TeacherDomainServiceTest {
    private lateinit var teacherAdaptor: TeacherAdaptor
    private lateinit var teacherDocumentAdaptor: TeacherDocumentAdaptor
    private lateinit var userRoleAdaptor: UserRoleAdaptor
    private lateinit var teacherDomainService: TeacherDomainService

    private lateinit var user: User

    @BeforeEach
    fun setUp() {
        teacherAdaptor = mockk()
        teacherDocumentAdaptor = mockk()
        userRoleAdaptor = mockk()
        teacherDomainService = TeacherDomainService(teacherAdaptor, teacherDocumentAdaptor, userRoleAdaptor)
        user = mockk<User>()
        every { user.id } returns "user-id"
    }

    @Nested
    inner class Register {
        @Test
        fun `교사를 등록한다`() {
            val slot = slot<Teacher>()
            every { teacherAdaptor.existsByUserId("user-id") } returns false
            every { teacherAdaptor.save(capture(slot)) } answers { slot.captured }

            val result = teacherDomainService.register(user = user)

            assertEquals(user, result.user)
        }

        @Test
        fun `이미 등록된 교사이면 TeacherAlreadyExistsException이 발생한다`() {
            every { teacherAdaptor.existsByUserId("user-id") } returns true

            assertThrows<TeacherAlreadyExistsException> {
                teacherDomainService.register(user = user)
            }
        }
    }
}
