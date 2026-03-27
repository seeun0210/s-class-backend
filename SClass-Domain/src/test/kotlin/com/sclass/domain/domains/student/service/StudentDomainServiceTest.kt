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
        fun `학생을 등록한다`() {
            val slot = slot<Student>()
            every { studentAdaptor.existsByUserId("user-id") } returns false
            every { studentAdaptor.save(capture(slot)) } answers { slot.captured }

            val result = studentDomainService.register(user = user)

            assertEquals(user, result.user)
        }

        @Test
        fun `이미 등록된 학생이면 StudentAlreadyExistsException이 발생한다`() {
            every { studentAdaptor.existsByUserId("user-id") } returns true

            assertThrows<StudentAlreadyExistsException> {
                studentDomainService.register(user = user)
            }
        }
    }
}
