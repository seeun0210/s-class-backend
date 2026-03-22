package com.sclass.domain.domains.teacher.adaptor

import com.sclass.domain.domains.teacher.domain.TeacherDocument
import com.sclass.domain.domains.teacher.domain.TeacherDocumentType
import com.sclass.domain.domains.teacher.exception.TeacherDocumentNotFoundException
import com.sclass.domain.domains.teacher.repository.TeacherDocumentRepository
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

class TeacherDocumentAdaptorTest {
    private lateinit var teacherDocumentRepository: TeacherDocumentRepository
    private lateinit var teacherDocumentAdaptor: TeacherDocumentAdaptor

    @BeforeEach
    fun setUp() {
        teacherDocumentRepository = mockk()
        teacherDocumentAdaptor = TeacherDocumentAdaptor(teacherDocumentRepository)
    }

    @Nested
    inner class FindById {
        @Test
        fun `존재하는 id로 조회하면 서류를 반환한다`() {
            val document = mockk<TeacherDocument>()
            every { teacherDocumentRepository.findById("doc-id") } returns Optional.of(document)

            val result = teacherDocumentAdaptor.findById("doc-id")

            assertEquals(document, result)
        }

        @Test
        fun `존재하지 않는 id로 조회하면 TeacherDocumentNotFoundException이 발생한다`() {
            every { teacherDocumentRepository.findById("unknown-id") } returns Optional.empty()

            assertThrows<TeacherDocumentNotFoundException> {
                teacherDocumentAdaptor.findById("unknown-id")
            }
        }
    }

    @Nested
    inner class FindByIdOrNull {
        @Test
        fun `존재하면 서류를 반환한다`() {
            val document = mockk<TeacherDocument>()
            every { teacherDocumentRepository.findById("doc-id") } returns Optional.of(document)

            val result = teacherDocumentAdaptor.findByIdOrNull("doc-id")

            assertEquals(document, result)
        }

        @Test
        fun `존재하지 않으면 null을 반환한다`() {
            every { teacherDocumentRepository.findById("unknown-id") } returns Optional.empty()

            val result = teacherDocumentAdaptor.findByIdOrNull("unknown-id")

            assertNull(result)
        }
    }

    @Nested
    inner class FindAllByTeacherId {
        @Test
        fun `교사의 서류 목록을 반환한다`() {
            val documents = listOf(mockk<TeacherDocument>(), mockk<TeacherDocument>())
            every { teacherDocumentRepository.findAllByTeacherId("teacher-id") } returns documents

            val result = teacherDocumentAdaptor.findAllByTeacherId("teacher-id")

            assertEquals(2, result.size)
        }

        @Test
        fun `서류가 없으면 빈 리스트를 반환한다`() {
            every { teacherDocumentRepository.findAllByTeacherId("teacher-id") } returns emptyList()

            val result = teacherDocumentAdaptor.findAllByTeacherId("teacher-id")

            assertTrue(result.isEmpty())
        }
    }

    @Nested
    inner class FindByTeacherIdAndDocumentType {
        @Test
        fun `교사의 특정 타입 서류를 반환한다`() {
            val document = mockk<TeacherDocument>()
            every {
                teacherDocumentRepository.findByTeacherIdAndDocumentType("teacher-id", TeacherDocumentType.APPLICATION)
            } returns document

            val result = teacherDocumentAdaptor.findByTeacherIdAndDocumentType("teacher-id", TeacherDocumentType.APPLICATION)

            assertEquals(document, result)
        }

        @Test
        fun `존재하지 않으면 null을 반환한다`() {
            every {
                teacherDocumentRepository.findByTeacherIdAndDocumentType("teacher-id", TeacherDocumentType.APPLICATION)
            } returns null

            val result = teacherDocumentAdaptor.findByTeacherIdAndDocumentType("teacher-id", TeacherDocumentType.APPLICATION)

            assertNull(result)
        }
    }

    @Nested
    inner class Save {
        @Test
        fun `서류 저장을 repository에 위임한다`() {
            val document = mockk<TeacherDocument>()
            every { teacherDocumentRepository.save(document) } returns document

            val result = teacherDocumentAdaptor.save(document)

            assertEquals(document, result)
            verify { teacherDocumentRepository.save(document) }
        }
    }
}
