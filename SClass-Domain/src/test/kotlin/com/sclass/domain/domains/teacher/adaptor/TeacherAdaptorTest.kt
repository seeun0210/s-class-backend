package com.sclass.domain.domains.teacher.adaptor

import com.sclass.domain.domains.teacher.domain.Teacher
import com.sclass.domain.domains.teacher.exception.TeacherNotFoundException
import com.sclass.domain.domains.teacher.repository.TeacherRepository
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

class TeacherAdaptorTest {
    private lateinit var teacherRepository: TeacherRepository
    private lateinit var teacherAdaptor: TeacherAdaptor

    @BeforeEach
    fun setUp() {
        teacherRepository = mockk()
        teacherAdaptor = TeacherAdaptor(teacherRepository)
    }

    @Nested
    inner class FindById {
        @Test
        fun `존재하는 id로 조회하면 교사를 반환한다`() {
            val teacher = mockk<Teacher>()
            every { teacherRepository.findById("teacher-id") } returns Optional.of(teacher)

            val result = teacherAdaptor.findById("teacher-id")

            assertEquals(teacher, result)
        }

        @Test
        fun `존재하지 않는 id로 조회하면 TeacherNotFoundException이 발생한다`() {
            every { teacherRepository.findById("unknown-id") } returns Optional.empty()

            assertThrows<TeacherNotFoundException> {
                teacherAdaptor.findById("unknown-id")
            }
        }
    }

    @Nested
    inner class FindByIdOrNull {
        @Test
        fun `존재하는 id로 조회하면 교사를 반환한다`() {
            val teacher = mockk<Teacher>()
            every { teacherRepository.findById("teacher-id") } returns Optional.of(teacher)

            val result = teacherAdaptor.findByIdOrNull("teacher-id")

            assertEquals(teacher, result)
        }

        @Test
        fun `존재하지 않는 id로 조회하면 null을 반환한다`() {
            every { teacherRepository.findById("unknown-id") } returns Optional.empty()

            val result = teacherAdaptor.findByIdOrNull("unknown-id")

            assertNull(result)
        }
    }

    @Nested
    inner class FindByUserId {
        @Test
        fun `존재하는 userId로 조회하면 교사를 반환한다`() {
            val teacher = mockk<Teacher>()
            every { teacherRepository.findByUserId("user-id") } returns teacher

            val result = teacherAdaptor.findByUserId("user-id")

            assertEquals(teacher, result)
        }

        @Test
        fun `존재하지 않으면 TeacherNotFoundException이 발생한다`() {
            every { teacherRepository.findByUserId("user-id") } returns null

            assertThrows<TeacherNotFoundException> {
                teacherAdaptor.findByUserId("user-id")
            }
        }
    }

    @Nested
    inner class FindByUserIdOrNull {
        @Test
        fun `존재하면 교사를 반환한다`() {
            val teacher = mockk<Teacher>()
            every { teacherRepository.findByUserId("user-id") } returns teacher

            val result = teacherAdaptor.findByUserIdOrNull("user-id")

            assertEquals(teacher, result)
        }

        @Test
        fun `존재하지 않으면 null을 반환한다`() {
            every { teacherRepository.findByUserId("user-id") } returns null

            val result = teacherAdaptor.findByUserIdOrNull("user-id")

            assertNull(result)
        }
    }

    @Nested
    inner class ExistsByUserId {
        @Test
        fun `교사가 존재하면 true를 반환한다`() {
            every { teacherRepository.existsByUserId("user-id") } returns true

            val result = teacherAdaptor.existsByUserId("user-id")

            assertTrue(result)
        }

        @Test
        fun `교사가 없으면 false를 반환한다`() {
            every { teacherRepository.existsByUserId("user-id") } returns false

            val result = teacherAdaptor.existsByUserId("user-id")

            assertFalse(result)
        }
    }

    @Nested
    inner class Save {
        @Test
        fun `교사 저장을 repository에 위임한다`() {
            val teacher = mockk<Teacher>()
            every { teacherRepository.save(teacher) } returns teacher

            val result = teacherAdaptor.save(teacher)

            assertEquals(teacher, result)
            verify { teacherRepository.save(teacher) }
        }
    }
}
