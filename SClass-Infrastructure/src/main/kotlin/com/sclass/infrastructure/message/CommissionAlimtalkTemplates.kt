package com.sclass.infrastructure.message

import com.sclass.infrastructure.message.dto.AlimtalkRequest
import com.sclass.infrastructure.message.dto.AlimtalkTemplate

object CommissionAlimtalkTemplates {
    private const val TEACHER_BASE_URL = "https://sclass.aura.co.kr/teacher/commissions"
    private const val STUDENT_BASE_URL = "https://sclass.aura.co.kr/student/commissions"

    private fun teacherButton(commissionId: String) =
        AlimtalkRequest.Button(
            name = "의뢰 확인하기",
            linkMobile = "$TEACHER_BASE_URL/$commissionId",
            linkPc = "$TEACHER_BASE_URL/$commissionId",
        )

    private fun studentButton(
        name: String = "의뢰 확인하기",
        commissionId: String,
    ) = AlimtalkRequest.Button(
        name = name,
        linkMobile = "$STUDENT_BASE_URL/$commissionId",
        linkPc = "$STUDENT_BASE_URL/$commissionId",
    )

    fun commissionAssigned(
        teacherName: String,
        studentName: String,
        subject: String,
        createdAt: String,
        commissionId: String,
    ) = AlimtalkTemplate(
        templateCode = "CMSNEWASSIGNED",
        content = "[S-Class] 새 의뢰가 배정되었습니다\n\n선생님: $teacherName\n학생: $studentName\n과목: $subject\n요청일시: $createdAt\n\n72시간 이내에 응답해주세요.",
        buttons = listOf(teacherButton(commissionId)),
    )

    fun topicSuggested(
        studentName: String,
        commissionId: String,
    ) = AlimtalkTemplate(
        templateCode = "COMMTOPSUGGEST",
        content = "[S-Class] 탐구 주제가 추천되었습니다\n\n학생: $studentName\n\n주제를 확인하고 선택해주세요.",
        buttons = listOf(studentButton(name = "주제 확인하기", commissionId = commissionId)),
    )

    fun additionalInfoRequested(
        studentName: String,
        requestContent: String,
        commissionId: String,
    ) = AlimtalkTemplate(
        templateCode = "COMMTOPREQUEST",
        content = "[S-Class] 추가 자료 요청이 도착했습니다\n\n학생: $studentName\n요청 내용: $requestContent\n\n추가 자료를 업로드해주세요.",
        buttons = listOf(studentButton(commissionId = commissionId)),
    )

    fun ticketResolved(
        teacherName: String,
        ticketType: String,
        commissionId: String,
    ) = AlimtalkTemplate(
        templateCode = "COMMTKRESOLVED",
        content = "[S-Class] 지원 티켓이 처리되었습니다\n\n선생님: $teacherName\n티켓 유형: $ticketType\n\n의뢰를 확인해주세요.",
        buttons = listOf(teacherButton(commissionId)),
    )

    fun noResponseReminder(
        teacherName: String,
        studentName: String,
        elapsedTime: String,
        commissionId: String,
    ) = AlimtalkTemplate(
        templateCode = "COMMNORESP",
        content = "[S-Class] 의뢰 응답 기한이 다가오고 있습니다\n\n선생님: $teacherName\n학생: $studentName\n경과 시간:$elapsedTime\n\n빠른 응답 부탁드립니다.",
        buttons = listOf(teacherButton(commissionId)),
    )

    fun inactivityReminder(
        teacherName: String,
        studentName: String,
        inactiveDays: Int,
        lastActivityAt: String,
        commissionId: String,
    ) = AlimtalkTemplate(
        templateCode = "COMMINACTIVE",
        content = "[S-Class] 의뢰 활동이 ${inactiveDays}일간 없었습니다\n\n선생님: $teacherName\n학생: $studentName\n마지막 활동: $lastActivityAt\n\n의뢰를 확인해주세요.",
        buttons = listOf(teacherButton(commissionId)),
    )
}
