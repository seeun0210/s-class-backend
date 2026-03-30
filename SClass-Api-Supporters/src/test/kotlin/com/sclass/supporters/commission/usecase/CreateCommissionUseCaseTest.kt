package com.sclass.supporters.commission.usecase

import com.sclass.domain.domains.commission.adaptor.CommissionAdaptor
import com.sclass.domain.domains.commission.adaptor.CommissionFileAdaptor
import com.sclass.domain.domains.commission.domain.ActivityType
import com.sclass.domain.domains.commission.domain.Commission
import com.sclass.domain.domains.commission.domain.CommissionStatus
import com.sclass.domain.domains.commission.domain.OutputFormat
import com.sclass.domain.domains.file.adaptor.FileAdaptor
import com.sclass.domain.domains.file.domain.File
import com.sclass.domain.domains.teacherassignment.adaptor.TeacherAssignmentAdaptor
import com.sclass.domain.domains.teacherassignment.domain.TeacherAssignment
import com.sclass.domain.domains.teacherassignment.exception.TeacherAssignmentNotFoundException
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.supporters.commission.dto.CreateCommissionRequest
import com.sclass.supporters.commission.dto.GuideInfoRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CreateCommissionUseCaseTest {
    private lateinit var commissionAdaptor: CommissionAdaptor
    private lateinit var commissionFileAdaptor: CommissionFileAdaptor
    private lateinit var teacherAssignmentAdaptor: TeacherAssignmentAdaptor
    private lateinit var fileAdaptor: FileAdaptor
    private lateinit var useCase: CreateCommissionUseCase

    @BeforeEach
    fun setUp() {
        commissionAdaptor = mockk()
        commissionFileAdaptor = mockk()
        teacherAssignmentAdaptor = mockk()
        fileAdaptor = mockk()
        useCase = CreateCommissionUseCase(commissionAdaptor, commissionFileAdaptor, teacherAssignmentAdaptor, fileAdaptor)
    }

    private fun createRequest(
        teacherUserId: String = "teacher-user-id-00000000001",
        fileIds: List<String>? = null,
    ) = CreateCommissionRequest(
        teacherUserId = teacherUserId,
        outputFormat = OutputFormat.REPORT,
        activityType = ActivityType.CAREER_EXPLORATION,
        guideInfo =
            GuideInfoRequest(
                subject = "미시경제학",
                volume = "A4 3매 이내",
                requiredElements = "신문 기사 활용",
                gradingCriteria = "경제학 기본원리 반영의 일관성(10), 경제 현상 분석의 체계성(30)",
                teacherEmphasis = "수요·공급 이론을 반드시 포함",
            ),
        fileIds = fileIds,
    )

    private fun mockTeacherAssignment() {
        every {
            teacherAssignmentAdaptor.findActiveByStudentUserIdAndPlatformAndOrganizationId(
                studentUserId = any(),
                platform = Platform.SUPPORTERS,
                organizationId = null,
            )
        } returns mockk<TeacherAssignment>()
    }

    @Test
    fun `의뢰를 정상 생성하면 CommissionResponse를 반환한다`() {
        mockTeacherAssignment()
        val commissionSlot = slot<Commission>()
        every { commissionAdaptor.save(capture(commissionSlot)) } answers { commissionSlot.captured }

        val result = useCase.execute("student-user-id-0000000001", createRequest())

        assertAll(
            { assertEquals(OutputFormat.REPORT, result.outputFormat) },
            { assertEquals(ActivityType.CAREER_EXPLORATION, result.activityType) },
            { assertEquals(CommissionStatus.REQUESTED, result.status) },
            { assertEquals("미시경제학", result.guideInfo.subject) },
            { assertEquals("A4 3매 이내", result.guideInfo.volume) },
        )
    }

    @Test
    fun `fileIds가 있으면 CommissionFile이 저장된다`() {
        mockTeacherAssignment()
        val commissionSlot = slot<Commission>()
        every { commissionAdaptor.save(capture(commissionSlot)) } answers { commissionSlot.captured }

        val mockFile = mockk<File>()
        every { fileAdaptor.findById(any()) } returns mockFile
        every { commissionFileAdaptor.saveAll(any()) } returns emptyList()

        useCase.execute("student-user-id-0000000001", createRequest(fileIds = listOf("file-id-1", "file-id-2")))

        verify(exactly = 2) { fileAdaptor.findById(any()) }
        verify { commissionFileAdaptor.saveAll(match { it.size == 2 }) }
    }

    @Test
    fun `fileIds가 없으면 CommissionFile 저장을 하지 않는다`() {
        mockTeacherAssignment()
        val commissionSlot = slot<Commission>()
        every { commissionAdaptor.save(capture(commissionSlot)) } answers { commissionSlot.captured }

        useCase.execute("student-user-id-0000000001", createRequest(fileIds = null))

        verify(exactly = 0) { commissionFileAdaptor.saveAll(any()) }
    }

    @Test
    fun `배정되지 않은 선생님에게 의뢰하면 예외가 발생한다`() {
        every {
            teacherAssignmentAdaptor.findActiveByStudentUserIdAndPlatformAndOrganizationId(
                studentUserId = any(),
                platform = Platform.SUPPORTERS,
                organizationId = null,
            )
        } throws TeacherAssignmentNotFoundException()

        assertThrows<TeacherAssignmentNotFoundException> {
            useCase.execute("student-user-id-0000000001", createRequest())
        }
    }
}
