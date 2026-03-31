package com.sclass.supporters.commission.usecase

import com.sclass.common.exception.BusinessException
import com.sclass.domain.domains.commission.adaptor.CommissionAdaptor
import com.sclass.domain.domains.commission.adaptor.MessageAdaptor
import com.sclass.domain.domains.commission.domain.ActivityType
import com.sclass.domain.domains.commission.domain.Commission
import com.sclass.domain.domains.commission.domain.CommissionStatus
import com.sclass.domain.domains.commission.domain.GuideInfo
import com.sclass.domain.domains.commission.domain.Message
import com.sclass.domain.domains.commission.domain.OutputFormat
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GetMessagesUseCaseTest {
    private lateinit var commissionAdaptor: CommissionAdaptor
    private lateinit var messageAdaptor: MessageAdaptor
    private lateinit var useCase: GetMessagesUseCase

    private val studentUserId = "student-user-id-0000000001"
    private val teacherUserId = "teacher-user-id-00000000001"

    @BeforeEach
    fun setUp() {
        commissionAdaptor = mockk()
        messageAdaptor = mockk()
        useCase = GetMessagesUseCase(commissionAdaptor, messageAdaptor)
    }

    private fun createCommission(id: Long = 1L) =
        Commission(
            id = id,
            studentUserId = studentUserId,
            teacherUserId = teacherUserId,
            outputFormat = OutputFormat.REPORT,
            activityType = ActivityType.CAREER_EXPLORATION,
            status = CommissionStatus.REQUESTED,
            guideInfo =
                GuideInfo(
                    subject = "미시경제학",
                    volume = "A4 3매",
                    gradingCriteria = "평가기준",
                    teacherEmphasis = "강조사항",
                ),
        )

    @Test
    fun `학생이 메시지 목록을 조회하면 MessageListResponse를 반환한다`() {
        val commission = createCommission()
        every { commissionAdaptor.findById(1L) } returns commission
        every { messageAdaptor.findByCommissionId(1L) } returns
            listOf(
                Message(id = 1L, commission = commission, senderId = teacherUserId, content = "추가 자료 부탁"),
                Message(id = 2L, commission = commission, senderId = studentUserId, content = "첨부합니다"),
            )

        val result = useCase.execute(studentUserId, 1L)

        assertEquals(2, result.messages.size)
    }

    @Test
    fun `선생님이 메시지 목록을 조회하면 MessageListResponse를 반환한다`() {
        val commission = createCommission()
        every { commissionAdaptor.findById(1L) } returns commission
        every { messageAdaptor.findByCommissionId(1L) } returns emptyList()

        val result = useCase.execute(teacherUserId, 1L)

        assertEquals(0, result.messages.size)
    }

    @Test
    fun `관련 없는 유저가 메시지를 조회하면 예외가 발생한다`() {
        val commission = createCommission()
        every { commissionAdaptor.findById(1L) } returns commission

        assertThrows<BusinessException> {
            useCase.execute("other-user-id-00000000001", 1L)
        }
    }
}
