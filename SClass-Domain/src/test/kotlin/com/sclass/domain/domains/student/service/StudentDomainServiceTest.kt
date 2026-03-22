package com.sclass.domain.domains.student.service

import com.sclass.domain.domains.student.adaptor.StudentAdaptor
import com.sclass.domain.domains.student.domain.Student
import com.sclass.domain.domains.student.exception.StudentAlreadyExistsException
import com.sclass.domain.domains.user.domain.User
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class StudentDomainServiceTest {
    private lateinit var studentAdaptor: StudentAdaptor
    private lateinit var studentDomainService: StudentDomainService

    private lateinit var user: User

    @BeforeEach
    fun setUp() {
        studentAdaptor = mockk()
        studentDomainService = StudentDomainService(studentAdaptor)
        user = mockk<User>()
        every { user.id } returns "user-id"
    }

    @Nested
    inner class Register {
        @Test
        fun `학생을 기관에 등록한다`() {
            val slot = slot<Student>()
            every { studentAdaptor.existsByUserIdAndOrganizationId("user-id", 1L) } returns false
            every { studentAdaptor.save(capture(slot)) } answers { slot.captured }

            val result = studentDomainService.register(user = user, organizationId = 1L)

            assertEquals(user, result.user)
            assertEquals(1L, result.organizationId)
        }

        @Test
        fun `이미 해당 기관에 등록된 학생이면 StudentAlreadyExistsException이 발생한다`() {
            every { studentAdaptor.existsByUserIdAndOrganizationId("user-id", 1L) } returns true

            assertThrows<StudentAlreadyExistsException> {
                studentDomainService.register(user = user, organizationId = 1L)
            }
        }
    }

    @Nested
    inner class FindAllByUserId {
        @Test
        fun `userId로 조회하면 해당 유저의 학생 목록을 반환한다`() {
            val students = listOf(mockk<Student>(), mockk<Student>())
            every { studentAdaptor.findAllByUserId("user-id") } returns students

            val result = studentDomainService.findAllByUserId("user-id")

            assertEquals(2, result.size)
            assertEquals(students, result)
        }
    }

    @Nested
    inner class FindAllByOrganizationId {
        @Test
        fun `기관의 학생 목록을 반환한다`() {
            val students = listOf(mockk<Student>(), mockk<Student>())
            every { studentAdaptor.findAllByOrganizationId(1L) } returns students

            val result = studentDomainService.findAllByOrganizationId(1L)

            assertEquals(2, result.size)
        }
    }
}
