package com.sclass.domain.domains.student.adaptor

import com.sclass.domain.domains.student.domain.Student
import com.sclass.domain.domains.student.dto.StudentSearchCondition
import com.sclass.domain.domains.student.dto.StudentWithRoles
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
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
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
    inner class FindByUserId {
        @Test
        fun `존재하는 userId로 조회하면 학생을 반환한다`() {
            val student = mockk<Student>()
            every { studentRepository.findByUserId("user-id") } returns student

            val result = studentAdaptor.findByUserId("user-id")

            assertEquals(student, result)
        }

        @Test
        fun `존재하지 않으면 StudentNotFoundException이 발생한다`() {
            every { studentRepository.findByUserId("user-id") } returns null

            assertThrows<StudentNotFoundException> {
                studentAdaptor.findByUserId("user-id")
            }
        }
    }

    @Nested
    inner class FindByUserIdOrNull {
        @Test
        fun `존재하면 학생을 반환한다`() {
            val student = mockk<Student>()
            every { studentRepository.findByUserId("user-id") } returns student

            val result = studentAdaptor.findByUserIdOrNull("user-id")

            assertEquals(student, result)
        }

        @Test
        fun `존재하지 않으면 null을 반환한다`() {
            every { studentRepository.findByUserId("user-id") } returns null

            val result = studentAdaptor.findByUserIdOrNull("user-id")

            assertNull(result)
        }
    }

    @Nested
    inner class ExistsByUserId {
        @Test
        fun `학생이 존재하면 true를 반환한다`() {
            every { studentRepository.existsByUserId("user-id") } returns true

            val result = studentAdaptor.existsByUserId("user-id")

            assertTrue(result)
        }

        @Test
        fun `학생이 없으면 false를 반환한다`() {
            every { studentRepository.existsByUserId("user-id") } returns false

            val result = studentAdaptor.existsByUserId("user-id")

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

    @Nested
    inner class SearchStudents {
        @Test
        fun `검색 조건과 페이지 정보를 repository에 위임한다`() {
            val condition = StudentSearchCondition(name = "홍길동")
            val pageable = PageRequest.of(0, 20)
            val studentWithRoles =
                mockk<StudentWithRoles> {
                    every { student } returns mockk()
                    every { roles } returns emptyList()
                }
            val page = PageImpl(listOf(studentWithRoles), pageable, 1L)

            every { studentRepository.searchStudents(condition, pageable) } returns page

            val result = studentAdaptor.searchStudents(condition, pageable)

            assertEquals(1, result.totalElements)
            assertEquals(1, result.content.size)
            verify { studentRepository.searchStudents(condition, pageable) }
        }

        @Test
        fun `결과가 없으면 빈 페이지를 반환한다`() {
            val condition = StudentSearchCondition()
            val pageable = PageRequest.of(0, 20)
            val page = PageImpl<StudentWithRoles>(emptyList(), pageable, 0L)

            every { studentRepository.searchStudents(condition, pageable) } returns page

            val result = studentAdaptor.searchStudents(condition, pageable)

            assertEquals(0, result.totalElements)
            assertTrue(result.content.isEmpty())
        }
    }
}
