package com.sclass.domain.domains.student.adaptor

import com.sclass.domain.domains.student.domain.Student
import com.sclass.domain.domains.student.exception.StudentNotFoundException
import com.sclass.domain.domains.student.repository.StudentRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Optional

class StudentAdaptorTest {
    private lateinit var studentRepository: StudentRepository
    private lateinit var studentAdaptor: StudentAdaptor

    @BeforeEach
    fun setUp() {
        studentRepository = mockk()
        studentAdaptor = StudentAdaptor(studentRepository)
    }

    @Nested
    inner class FindById {
        @Test
        fun `존재하는 id로 조회하면 학생을 반환한다`() {
            val student = mockk<Student>()
            every { studentRepository.findById("student-id") } returns Optional.of(student)

            val result = studentAdaptor.findById("student-id")

            assertEquals(student, result)
        }

        @Test
        fun `존재하지 않는 id로 조회하면 StudentNotFoundException이 발생한다`() {
            every { studentRepository.findById("unknown-id") } returns Optional.empty()

            assertThrows<StudentNotFoundException> {
                studentAdaptor.findById("unknown-id")
            }
        }
    }

    @Nested
    inner class FindByIdOrNull {
        @Test
        fun `존재하는 id로 조회하면 학생을 반환한다`() {
            val student = mockk<Student>()
            every { studentRepository.findById("student-id") } returns Optional.of(student)

            val result = studentAdaptor.findByIdOrNull("student-id")

            assertEquals(student, result)
        }

        @Test
        fun `존재하지 않는 id로 조회하면 null을 반환한다`() {
            every { studentRepository.findById("unknown-id") } returns Optional.empty()

            val result = studentAdaptor.findByIdOrNull("unknown-id")

            assertNull(result)
        }
    }

    @Nested
    inner class FindAllByUserId {
        @Test
        fun `userId로 조회하면 해당 유저의 학생 목록을 반환한다`() {
            val students = listOf(mockk<Student>(), mockk<Student>())
            every { studentRepository.findAllByUserId("user-id") } returns students

            val result = studentAdaptor.findAllByUserId("user-id")

            assertEquals(2, result.size)
            assertEquals(students, result)
        }

        @Test
        fun `학생이 없으면 빈 리스트를 반환한다`() {
            every { studentRepository.findAllByUserId("unknown-id") } returns emptyList()

            val result = studentAdaptor.findAllByUserId("unknown-id")

            assertTrue(result.isEmpty())
        }
    }

    @Nested
    inner class FindByUserIdAndOrganizationId {
        @Test
        fun `존재하는 userId와 organizationId로 조회하면 학생을 반환한다`() {
            val student = mockk<Student>()
            every { studentRepository.findByUserIdAndOrganizationId("user-id", 1L) } returns student

            val result = studentAdaptor.findByUserIdAndOrganizationId("user-id", 1L)

            assertEquals(student, result)
        }

        @Test
        fun `존재하지 않으면 StudentNotFoundException이 발생한다`() {
            every { studentRepository.findByUserIdAndOrganizationId("user-id", 1L) } returns null

            assertThrows<StudentNotFoundException> {
                studentAdaptor.findByUserIdAndOrganizationId("user-id", 1L)
            }
        }
    }

    @Nested
    inner class FindByUserIdAndOrganizationIdOrNull {
        @Test
        fun `존재하면 학생을 반환한다`() {
            val student = mockk<Student>()
            every { studentRepository.findByUserIdAndOrganizationId("user-id", 1L) } returns student

            val result = studentAdaptor.findByUserIdAndOrganizationIdOrNull("user-id", 1L)

            assertEquals(student, result)
        }

        @Test
        fun `존재하지 않으면 null을 반환한다`() {
            every { studentRepository.findByUserIdAndOrganizationId("user-id", 1L) } returns null

            val result = studentAdaptor.findByUserIdAndOrganizationIdOrNull("user-id", 1L)

            assertNull(result)
        }
    }

    @Nested
    inner class FindAllByOrganizationId {
        @Test
        fun `기관 id로 조회하면 해당 기관의 학생 목록을 반환한다`() {
            val students = listOf(mockk<Student>(), mockk<Student>())
            every { studentRepository.findAllByOrganizationId(1L) } returns students

            val result = studentAdaptor.findAllByOrganizationId(1L)

            assertEquals(2, result.size)
            assertEquals(students, result)
        }

        @Test
        fun `학생이 없으면 빈 리스트를 반환한다`() {
            every { studentRepository.findAllByOrganizationId(1L) } returns emptyList()

            val result = studentAdaptor.findAllByOrganizationId(1L)

            assertTrue(result.isEmpty())
        }
    }

    @Nested
    inner class ExistsByUserIdAndOrganizationId {
        @Test
        fun `학생이 존재하면 true를 반환한다`() {
            every { studentRepository.existsByUserIdAndOrganizationId("user-id", 1L) } returns true

            val result = studentAdaptor.existsByUserIdAndOrganizationId("user-id", 1L)

            assertTrue(result)
        }

        @Test
        fun `학생이 없으면 false를 반환한다`() {
            every { studentRepository.existsByUserIdAndOrganizationId("user-id", 1L) } returns false

            val result = studentAdaptor.existsByUserIdAndOrganizationId("user-id", 1L)

            assertFalse(result)
        }
    }

    @Nested
    inner class Save {
        @Test
        fun `학생 저장을 repository에 위임한다`() {
            val student = mockk<Student>()
            every { studentRepository.save(student) } returns student

            val result = studentAdaptor.save(student)

            assertEquals(student, result)
            verify { studentRepository.save(student) }
        }
    }
}
