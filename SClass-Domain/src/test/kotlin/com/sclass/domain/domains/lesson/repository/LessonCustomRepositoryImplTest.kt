package com.sclass.domain.domains.lesson.repository

import com.sclass.domain.config.DomainTestConfig
import com.sclass.domain.domains.lesson.domain.Lesson
import com.sclass.domain.domains.lesson.domain.LessonStatus
import com.sclass.domain.domains.lesson.domain.LessonType
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@DataJpaTest
@ActiveProfiles("test")
@Import(DomainTestConfig::class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ComponentScan(basePackages = ["com.sclass.domain"])
class LessonCustomRepositoryImplTest {
    @Autowired
    private lateinit var lessonRepository: LessonRepository

    @Autowired
    private lateinit var em: EntityManager

    @Test
    fun `학생이나 선생님의 기존 수업과 시간이 겹치면 충돌이다`() {
        persistLesson(
            studentUserId = STUDENT_USER_ID,
            assignedTeacherUserId = TEACHER_USER_ID,
            scheduledAt = LocalDateTime.of(2026, 5, 1, 20, 0),
        )
        em.flush()
        em.clear()

        assertTrue(
            lessonRepository.existsScheduleConflict(
                studentUserId = STUDENT_USER_ID,
                teacherUserId = "other-teacher-id",
                scheduledAt = LocalDateTime.of(2026, 5, 1, 20, 30),
                requestedDurationMinutes = 60L,
                excludeLessonId = 0L,
            ),
        )
        assertTrue(
            lessonRepository.existsScheduleConflict(
                studentUserId = "other-student-id",
                teacherUserId = TEACHER_USER_ID,
                scheduledAt = LocalDateTime.of(2026, 5, 1, 20, 30),
                requestedDurationMinutes = 60L,
                excludeLessonId = 0L,
            ),
        )
    }

    @Test
    fun `기존 수업과 시간이 딱 붙으면 충돌이 아니다`() {
        persistLesson(
            studentUserId = STUDENT_USER_ID,
            assignedTeacherUserId = TEACHER_USER_ID,
            scheduledAt = LocalDateTime.of(2026, 5, 1, 19, 0),
        )
        persistLesson(
            studentUserId = STUDENT_USER_ID,
            assignedTeacherUserId = TEACHER_USER_ID,
            scheduledAt = LocalDateTime.of(2026, 5, 1, 21, 0),
        )
        em.flush()
        em.clear()

        assertFalse(
            lessonRepository.existsScheduleConflict(
                studentUserId = STUDENT_USER_ID,
                teacherUserId = TEACHER_USER_ID,
                scheduledAt = LocalDateTime.of(2026, 5, 1, 20, 0),
                requestedDurationMinutes = 60L,
                excludeLessonId = 0L,
            ),
        )
    }

    @Test
    fun `수정 대상 수업은 충돌 검사에서 제외한다`() {
        val lesson =
            persistLesson(
                studentUserId = STUDENT_USER_ID,
                assignedTeacherUserId = TEACHER_USER_ID,
                scheduledAt = LocalDateTime.of(2026, 5, 1, 20, 0),
            )
        em.flush()
        em.clear()

        assertFalse(
            lessonRepository.existsScheduleConflict(
                studentUserId = STUDENT_USER_ID,
                teacherUserId = TEACHER_USER_ID,
                scheduledAt = LocalDateTime.of(2026, 5, 1, 20, 0),
                requestedDurationMinutes = 60L,
                excludeLessonId = lesson.id,
            ),
        )
    }

    @Test
    fun `대타가 있으면 대타 선생님 기준으로 충돌을 검사한다`() {
        persistLesson(
            studentUserId = "other-student-id",
            assignedTeacherUserId = TEACHER_USER_ID,
            substituteTeacherUserId = SUBSTITUTE_TEACHER_USER_ID,
            scheduledAt = LocalDateTime.of(2026, 5, 1, 20, 0),
        )
        em.flush()
        em.clear()

        assertFalse(
            lessonRepository.existsScheduleConflict(
                studentUserId = STUDENT_USER_ID,
                teacherUserId = TEACHER_USER_ID,
                scheduledAt = LocalDateTime.of(2026, 5, 1, 20, 30),
                requestedDurationMinutes = 60L,
                excludeLessonId = 0L,
            ),
        )
        assertTrue(
            lessonRepository.existsScheduleConflict(
                studentUserId = STUDENT_USER_ID,
                teacherUserId = SUBSTITUTE_TEACHER_USER_ID,
                scheduledAt = LocalDateTime.of(2026, 5, 1, 20, 30),
                requestedDurationMinutes = 60L,
                excludeLessonId = 0L,
            ),
        )
    }

    @Test
    fun `완료되거나 취소된 수업은 충돌 대상이 아니다`() {
        persistLesson(
            studentUserId = STUDENT_USER_ID,
            assignedTeacherUserId = TEACHER_USER_ID,
            scheduledAt = LocalDateTime.of(2026, 5, 1, 20, 0),
            status = LessonStatus.COMPLETED,
        )
        persistLesson(
            studentUserId = STUDENT_USER_ID,
            assignedTeacherUserId = TEACHER_USER_ID,
            scheduledAt = LocalDateTime.of(2026, 5, 1, 20, 30),
            status = LessonStatus.CANCELLED,
        )
        em.flush()
        em.clear()

        assertFalse(
            lessonRepository.existsScheduleConflict(
                studentUserId = STUDENT_USER_ID,
                teacherUserId = TEACHER_USER_ID,
                scheduledAt = LocalDateTime.of(2026, 5, 1, 20, 0),
                requestedDurationMinutes = 60L,
                excludeLessonId = 0L,
            ),
        )
    }

    private fun persistLesson(
        studentUserId: String,
        assignedTeacherUserId: String,
        scheduledAt: LocalDateTime,
        substituteTeacherUserId: String? = null,
        status: LessonStatus = LessonStatus.SCHEDULED,
    ): Lesson {
        val lesson =
            Lesson(
                lessonType = LessonType.COURSE,
                enrollmentId = 1L,
                studentUserId = studentUserId,
                assignedTeacherUserId = assignedTeacherUserId,
                substituteTeacherUserId = substituteTeacherUserId,
                lessonNumber = 1,
                name = "수학 수업",
                scheduledAt = scheduledAt,
                status = status,
            )
        em.persist(lesson)
        return lesson
    }

    private companion object {
        const val STUDENT_USER_ID = "student-user-id"
        const val TEACHER_USER_ID = "teacher-user-id"
        const val SUBSTITUTE_TEACHER_USER_ID = "substitute-teacher-id"
    }
}
