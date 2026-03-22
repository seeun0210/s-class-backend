package com.sclass.domain.domains.student.adaptor

import com.sclass.domain.domains.student.domain.StudentDocument
import com.sclass.domain.domains.student.domain.StudentDocumentType
import com.sclass.domain.domains.student.exception.StudentDocumentNotFoundException
import com.sclass.domain.domains.student.repository.StudentDocumentRepository
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

class StudentDocumentAdaptorTest {
    private lateinit var studentDocumentRepository: StudentDocumentRepository
    private lateinit var studentDocumentAdaptor: StudentDocumentAdaptor

    @BeforeEach
    fun setUp() {
        studentDocumentRepository = mockk()
        studentDocumentAdaptor = StudentDocumentAdaptor(studentDocumentRepository)
    }

    @Nested
    inner class FindById {
        @Test
        fun `존재하는 id로 조회하면 서류를 반환한다`() {
            val document = mockk<StudentDocument>()
            every { studentDocumentRepository.findById("doc-id") } returns Optional.of(document)

            val result = studentDocumentAdaptor.findById("doc-id")

            assertEquals(document, result)
        }

        @Test
        fun `존재하지 않는 id로 조회하면 StudentDocumentNotFoundException이 발생한다`() {
            every { studentDocumentRepository.findById("unknown-id") } returns Optional.empty()

            assertThrows<StudentDocumentNotFoundException> {
                studentDocumentAdaptor.findById("unknown-id")
            }
        }
    }

    @Nested
    inner class FindByIdOrNull {
        @Test
        fun `존재하면 서류를 반환한다`() {
            val document = mockk<StudentDocument>()
            every { studentDocumentRepository.findById("doc-id") } returns Optional.of(document)

            val result = studentDocumentAdaptor.findByIdOrNull("doc-id")

            assertEquals(document, result)
        }

        @Test
        fun `존재하지 않으면 null을 반환한다`() {
            every { studentDocumentRepository.findById("unknown-id") } returns Optional.empty()

            val result = studentDocumentAdaptor.findByIdOrNull("unknown-id")

            assertNull(result)
        }
    }

    @Nested
    inner class FindAllByStudentId {
        @Test
        fun `학생의 서류 목록을 반환한다`() {
            val documents = listOf(mockk<StudentDocument>(), mockk<StudentDocument>())
            every { studentDocumentRepository.findAllByStudentId("student-id") } returns documents

            val result = studentDocumentAdaptor.findAllByStudentId("student-id")

            assertEquals(2, result.size)
        }

        @Test
        fun `서류가 없으면 빈 리스트를 반환한다`() {
            every { studentDocumentRepository.findAllByStudentId("student-id") } returns emptyList()

            val result = studentDocumentAdaptor.findAllByStudentId("student-id")

            assertTrue(result.isEmpty())
        }
    }

    @Nested
    inner class FindAllByStudentIdAndDocumentType {
        @Test
        fun `학생의 특정 타입 서류 목록을 반환한다`() {
            val documents = listOf(mockk<StudentDocument>(), mockk<StudentDocument>())
            every {
                studentDocumentRepository.findAllByStudentIdAndDocumentType("student-id", StudentDocumentType.REGISTRATION_RECEIPT)
            } returns documents

            val result = studentDocumentAdaptor.findAllByStudentIdAndDocumentType("student-id", StudentDocumentType.REGISTRATION_RECEIPT)

            assertEquals(2, result.size)
        }
    }

    @Nested
    inner class Save {
        @Test
        fun `서류 저장을 repository에 위임한다`() {
            val document = mockk<StudentDocument>()
            every { studentDocumentRepository.save(document) } returns document

            val result = studentDocumentAdaptor.save(document)

            assertEquals(document, result)
            verify { studentDocumentRepository.save(document) }
        }
    }
}
