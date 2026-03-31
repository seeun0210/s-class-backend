package com.sclass.supporters.commission.usecase

import com.sclass.common.exception.BusinessException
import com.sclass.domain.domains.commission.adaptor.CommissionAdaptor
import com.sclass.domain.domains.commission.adaptor.CommissionSupportTicketAdaptor
import com.sclass.domain.domains.commission.domain.ActivityType
import com.sclass.domain.domains.commission.domain.Commission
import com.sclass.domain.domains.commission.domain.CommissionStatus
import com.sclass.domain.domains.commission.domain.CommissionSupportTicket
import com.sclass.domain.domains.commission.domain.GuideInfo
import com.sclass.domain.domains.commission.domain.OutputFormat
import com.sclass.domain.domains.commission.domain.SupportTicketType
import com.sclass.domain.domains.commission.domain.TicketStatus
import com.sclass.supporters.commission.dto.CreateSupportTicketRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CreateSupportTicketUseCaseTest {
    private lateinit var commissionAdaptor: CommissionAdaptor
    private lateinit var commissionSupportTicketAdaptor: CommissionSupportTicketAdaptor
    private lateinit var useCase: CreateSupportTicketUseCase

    private val studentUserId = "student-user-id-0000000001"
    private val teacherUserId = "teacher-user-id-00000000001"

    @BeforeEach
    fun setUp() {
        commissionAdaptor = mockk()
        commissionSupportTicketAdaptor = mockk()
        useCase = CreateSupportTicketUseCase(commissionAdaptor, commissionSupportTicketAdaptor)
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
    fun `선생님이 지원 티켓을 생성하면 OPEN 상태로 생성된다`() {
        val commission = createCommission()
        every { commissionAdaptor.findById(1L) } returns commission

        val ticketSlot = slot<CommissionSupportTicket>()
        every { commissionSupportTicketAdaptor.save(capture(ticketSlot)) } answers { ticketSlot.captured }

        val result =
            useCase.execute(
                teacherUserId,
                1L,
                CreateSupportTicketRequest(type = SupportTicketType.TOPIC_SUGGESTION, reason = "적합한 주제가 없습니다"),
            )

        assertAll(
            { assertEquals(SupportTicketType.TOPIC_SUGGESTION, result.type) },
            { assertEquals("적합한 주제가 없습니다", result.reason) },
            { assertEquals(TicketStatus.OPEN, result.status) },
        )
    }

    @Test
    fun `학생이 지원 티켓을 생성하면 예외가 발생한다`() {
        val commission = createCommission()
        every { commissionAdaptor.findById(1L) } returns commission

        assertThrows<BusinessException> {
            useCase.execute(
                studentUserId,
                1L,
                CreateSupportTicketRequest(type = SupportTicketType.TOPIC_SUGGESTION, reason = "test"),
            )
        }
    }

    @Test
    fun `관련 없는 유저가 지원 티켓을 생성하면 예외가 발생한다`() {
        val commission = createCommission()
        every { commissionAdaptor.findById(1L) } returns commission

        assertThrows<BusinessException> {
            useCase.execute(
                "other-user-id-00000000001",
                1L,
                CreateSupportTicketRequest(type = SupportTicketType.TOPIC_SUGGESTION, reason = "test"),
            )
        }
    }
}
