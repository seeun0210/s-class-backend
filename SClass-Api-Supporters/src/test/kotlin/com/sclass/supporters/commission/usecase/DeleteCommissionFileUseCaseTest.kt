package com.sclass.supporters.commission.usecase

import com.sclass.domain.domains.commission.adaptor.CommissionAdaptor
import com.sclass.domain.domains.commission.adaptor.CommissionFileAdaptor
import com.sclass.domain.domains.commission.domain.ActivityType
import com.sclass.domain.domains.commission.domain.Commission
import com.sclass.domain.domains.commission.domain.CommissionFile
import com.sclass.domain.domains.commission.domain.CommissionStatus
import com.sclass.domain.domains.commission.domain.GuideInfo
import com.sclass.domain.domains.commission.domain.OutputFormat
import com.sclass.domain.domains.commission.exception.CommissionUnauthorizedAccessException
import com.sclass.domain.domains.file.domain.File
import com.sclass.domain.domains.file.domain.FileType
import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.domain.domains.lesson.domain.Lesson
import com.sclass.domain.domains.lesson.domain.LessonStatus
import com.sclass.domain.domains.lesson.domain.LessonType
import com.sclass.domain.domains.lesson.exception.LessonAlreadyCompletedException
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DeleteCommissionFileUseCaseTest {
    private lateinit var commissionAdaptor: CommissionAdaptor
    private lateinit var commissionFileAdaptor: CommissionFileAdaptor
    private lateinit var lessonAdaptor: LessonAdaptor
    private lateinit var useCase: DeleteCommissionFileUseCase

    private val commissionId = 1L
    private val lessonId = 100L
    private val commissionFileId = 10L
    private val studentUserId = "student-user-id-0000000001"
    private val teacherUserId = "teacher-user-id-00000000001"
    private val substituteTeacherUserId = "teacher-user-id-00000000002"

    @BeforeEach
    fun setUp() {
        commissionAdaptor = mockk()
        commissionFileAdaptor = mockk()
        lessonAdaptor = mockk()
        useCase = DeleteCommissionFileUseCase(commissionAdaptor, commissionFileAdaptor, lessonAdaptor)
    }

    private fun commission(acceptedLessonId: Long? = lessonId) =
        Commission(
            id = commissionId,
            studentUserId = studentUserId,
            teacherUserId = teacherUserId,
            commissionPolicyId = "policy-id-00000000000001",
            outputFormat = OutputFormat.REPORT,
            activityType = ActivityType.CAREER_EXPLORATION,
            status = CommissionStatus.ACCEPTED,
            guideInfo =
                GuideInfo(
                    subject = "미시경제학",
                    volume = "A4 3매",
                    gradingCriteria = "평가기준",
                    teacherEmphasis = "강조사항",
                ),
            selectedTopicId = 10L,
            acceptedLessonId = acceptedLessonId,
        )

    private fun lesson(
        status: LessonStatus = LessonStatus.SCHEDULED,
        substituteTeacherUserId: String? = null,
    ) = Lesson(
        id = lessonId,
        lessonType = LessonType.COMMISSION,
        enrollmentId = 20L,
        sourceCommissionId = commissionId,
        studentUserId = studentUserId,
        assignedTeacherUserId = teacherUserId,
        substituteTeacherUserId = substituteTeacherUserId,
        name = "미시경제학",
        status = status,
    )

    private fun file(id: String = "task-submit-file-00000001") =
        File.create(
            id = id,
            originalFilename = "$id.pdf",
            storedFilename = "commissions/$id.pdf",
            mimeType = "application/pdf",
            fileSize = 1024L,
            fileType = FileType.TASK_SUBMISSION,
            uploadedBy = teacherUserId,
        )

    @Test
    fun `대체 선생님이 배정된 수업이면 대체 선생님이 제출 파일을 삭제할 수 있다`() {
        val commission = commission()
        val lesson = lesson(substituteTeacherUserId = substituteTeacherUserId)
        val commissionFile = CommissionFile(id = commissionFileId, commission = commission, file = file())

        every { commissionAdaptor.findById(commissionId) } returns commission
        every { lessonAdaptor.findById(lessonId) } returns lesson
        every { commissionFileAdaptor.findById(commissionFileId) } returns commissionFile
        every { commissionFileAdaptor.delete(commissionFile) } just runs
        every { commissionFileAdaptor.findByCommissionId(commissionId) } returns emptyList()

        val result = useCase.execute(substituteTeacherUserId, commissionId, commissionFileId)

        assertAll(
            { assertEquals(commissionId, result.id) },
            { assertEquals(emptyList<Long>(), result.commissionFiles.map { it.id }) },
        )
        verify { commissionFileAdaptor.delete(commissionFile) }
    }

    @Test
    fun `대체 선생님이 배정된 수업이면 원래 선생님은 제출 파일을 삭제할 수 없다`() {
        val commission = commission()
        val lesson = lesson(substituteTeacherUserId = substituteTeacherUserId)

        every { commissionAdaptor.findById(commissionId) } returns commission
        every { lessonAdaptor.findById(lessonId) } returns lesson

        assertThrows<CommissionUnauthorizedAccessException> {
            useCase.execute(teacherUserId, commissionId, commissionFileId)
        }

        verify(exactly = 0) { commissionFileAdaptor.findById(any()) }
        verify(exactly = 0) { commissionFileAdaptor.delete(any()) }
    }

    @Test
    fun `accepted lesson이 없으면 기존 commission 담당 선생님이 제출 파일을 삭제할 수 있다`() {
        val commission = commission(acceptedLessonId = null)
        val commissionFile = CommissionFile(id = commissionFileId, commission = commission, file = file())

        every { commissionAdaptor.findById(commissionId) } returns commission
        every { commissionFileAdaptor.findById(commissionFileId) } returns commissionFile
        every { commissionFileAdaptor.delete(commissionFile) } just runs
        every { commissionFileAdaptor.findByCommissionId(commissionId) } returns emptyList()

        useCase.execute(teacherUserId, commissionId, commissionFileId)

        verify(exactly = 0) { lessonAdaptor.findById(any()) }
        verify { commissionFileAdaptor.delete(commissionFile) }
    }

    @Test
    fun `완료된 수업이면 제출 파일 삭제가 불가하다`() {
        val commission = commission()
        val lesson = lesson(status = LessonStatus.COMPLETED)

        every { commissionAdaptor.findById(commissionId) } returns commission
        every { lessonAdaptor.findById(lessonId) } returns lesson

        assertThrows<LessonAlreadyCompletedException> {
            useCase.execute(teacherUserId, commissionId, commissionFileId)
        }

        verify(exactly = 0) { commissionFileAdaptor.findById(any()) }
        verify(exactly = 0) { commissionFileAdaptor.delete(any()) }
    }
}
