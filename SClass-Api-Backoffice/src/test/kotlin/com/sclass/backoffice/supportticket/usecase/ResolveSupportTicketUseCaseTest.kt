package com.sclass.backoffice.supportticket.usecase

import com.sclass.backoffice.supportticket.dto.ResolveSupportTicketRequest
import com.sclass.domain.domains.commission.adaptor.CommissionSupportTicketAdaptor
import com.sclass.domain.domains.commission.domain.ActivityType
import com.sclass.domain.domains.commission.domain.Commission
import com.sclass.domain.domains.commission.domain.CommissionStatus
import com.sclass.domain.domains.commission.domain.CommissionSupportTicket
import com.sclass.domain.domains.commission.domain.GuideInfo
import com.sclass.domain.domains.commission.domain.OutputFormat
import com.sclass.domain.domains.commission.domain.SupportTicketType
import com.sclass.domain.domains.commission.domain.TicketStatus
import com.sclass.domain.domains.student.adaptor.StudentAdaptor
import com.sclass.domain.domains.student.domain.Student
import com.sclass.domain.domains.user.adaptor.UserAdaptor
import com.sclass.domain.domains.user.domain.AuthProvider
import com.sclass.domain.domains.user.domain.User
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ResolveSupportTicketUseCaseTest {
    private lateinit var commissionSupportTicketAdaptor: CommissionSupportTicketAdaptor
    private lateinit var userAdaptor: UserAdaptor
    private lateinit var studentAdaptor: StudentAdaptor
    private lateinit var useCase: ResolveSupportTicketUseCase

    private val studentUserId = "student-user-id-0000000001"
    private val teacherUserId = "teacher-user-id-00000000001"

    @BeforeEach
    fun setUp() {
        commissionSupportTicketAdaptor = mockk()
        userAdaptor = mockk()
        studentAdaptor = mockk()
        useCase = ResolveSupportTicketUseCase(commissionSupportTicketAdaptor, userAdaptor, studentAdaptor)
    }

    @Test
    fun `어드민이 티켓을 해결하면 RESOLVED 상태로 변경된다`() {
        val commission =
            Commission(
                id = 1L,
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

        val ticket =
            CommissionSupportTicket(
                id = 1L,
                commission = commission,
                type = SupportTicketType.TOPIC_SUGGESTION,
                reason = "적합한 주제가 없습니다",
            )

        val teacherUser = User(id = teacherUserId, email = "t@test.com", name = "김선생", authProvider = AuthProvider.EMAIL)
        val studentUser = User(id = studentUserId, email = "s@test.com", name = "이학생", authProvider = AuthProvider.EMAIL)
        val student = Student(id = "student-id-0000000000001", user = studentUser)

        every { commissionSupportTicketAdaptor.findById(1L) } returns ticket
        every { userAdaptor.findById(teacherUserId) } returns teacherUser
        every { studentAdaptor.findByUserIdWithUser(studentUserId) } returns student
        every { studentAdaptor.findDocumentsWithFileByStudentId("student-id-0000000000001") } returns emptyList()

        val request = ResolveSupportTicketRequest(response = "경제학 관련 주제 3건 등록했습니다")
        val result = useCase.execute(1L, request)

        assertAll(
            { assertEquals(TicketStatus.RESOLVED, result.status) },
            { assertEquals("경제학 관련 주제 3건 등록했습니다", result.response) },
            { assertEquals(TicketStatus.RESOLVED, ticket.status) },
            { assertEquals("경제학 관련 주제 3건 등록했습니다", ticket.response) },
        )
    }
}
