package com.sclass.infrastructure.message

import com.sclass.infrastructure.message.dto.AlimtalkRequest
import com.sclass.infrastructure.message.dto.AlimtalkTemplate

class CommissionAlimtalkTemplates(
    appBaseUrl: String,
) {
    private val teacherBaseUrl = "$appBaseUrl/teacher/commissions"
    private val studentBaseUrl = "$appBaseUrl/student/commissions"

    private fun teacherButton(
        name: String = "요청확인하기",
        commissionId: String,
    ) = AlimtalkRequest.Button(
        name = name,
        linkMobile = "$teacherBaseUrl/$commissionId",
        linkPc = "$teacherBaseUrl/$commissionId",
    )

    private fun studentButton(
        name: String = "의뢰 확인하기",
        commissionId: String,
    ) = AlimtalkRequest.Button(
        name = name,
        linkMobile = "$studentBaseUrl/$commissionId",
        linkPc = "$studentBaseUrl/$commissionId",
    )

    fun commissionAssigned(
        teacherName: String,
        studentName: String,
        subject: String,
        createdAt: String,
        commissionId: String,
    ) = AlimtalkTemplate(
        templateCode = "CMSNEWASSIGNED",
        content =
            "[S클래스] 새로운 탐구 의뢰가 배정되었습니다\n\n" +
                "$teacherName 선생님, 안녕하세요.\n\n" +
                "$studentName 학생의 탐구 의뢰가 선생님께 배정되었습니다.\n\n" +
                "■ 탐구분야: $subject\n" +
                "■ 접수일시: $createdAt\n\n" +
                "앱에서 의뢰 내용을 확인하고 응답해 주세요.\n\n" +
                "※ 본 메시지는 발신 전용으로, S클래스에서 자동 발송됩니다.",
        buttons = listOf(teacherButton(commissionId = commissionId)),
    )

    fun topicSuggested(
        studentName: String,
        commissionId: String,
    ) = AlimtalkTemplate(
        templateCode = "COMMTOPSUGGEST",
        content =
            "[S클래스] 탐구 주제 추천이 도착했습니다\n\n" +
                "${studentName}님, 안녕하세요.\n\n" +
                "앱에서 추천 주제를 확인하고 선택해 주세요.\n\n" +
                "※ 본 메시지는 발신 전용으로, S클래스에서 자동 발송됩니다.",
        buttons = listOf(studentButton(name = "주제 확인하기", commissionId = commissionId)),
    )

    fun additionalInfoRequested(
        studentName: String,
        requestContent: String,
        commissionId: String,
    ) = AlimtalkTemplate(
        templateCode = "COMMTOPREQUEST",
        content =
            "[S클래스] 탐구 주제 보강 요청이 도착했습니다.\n\n" +
                "${studentName}님, 안녕하세요.\n\n" +
                "등록하신 요청에 대해 선생님께서 자료 보강을 요청하셨습니다.\n" +
                "■ 요청내용: $requestContent\n\n" +
                "앱에서 요청 내용을 확인하고 답변해 주세요.",
        buttons = listOf(studentButton(name = "요청 확인하기", commissionId = commissionId)),
    )

    fun ticketResolved(
        teacherName: String,
        ticketType: String,
        commissionId: String,
    ) = AlimtalkTemplate(
        templateCode = "COMMTKRESOLVED",
        content =
            "[S클래스] 지원 요청이 처리되었습니다\n\n" +
                "$teacherName 선생님, 안녕하세요.\n\n" +
                "선생님의 $ticketType 요청이 처리 완료되었습니다.\n\n" +
                "앱에서 처리 결과를 확인해 주세요.",
        buttons = listOf(teacherButton(name = "처리 결과 확인하기", commissionId = commissionId)),
    )

    fun noResponseReminder(
        teacherName: String,
        studentName: String,
        elapsedTime: String,
        commissionId: String,
    ) = AlimtalkTemplate(
        templateCode = "COMMNORESP",
        content =
            "[S클래스] 배정된 의뢰 확인을 요청드립니다\n\n" +
                "$teacherName 선생님, 안녕하세요.\n\n" +
                "$studentName 학생의 탐구 의뢰가 배정된 지 ${elapsedTime}이 지났습니다.\n\n" +
                "학생이 선생님의 응답을 기다리고 있습니다. 앱에서 확인해 주세요.\n\n" +
                "※ 본 메시지는 발신 전용으로, S클래스에서 자동 발송됩니다.",
        buttons = listOf(teacherButton(name = "의뢰 확인하기", commissionId = commissionId)),
    )

    fun inactivityReminder(
        teacherName: String,
        studentName: String,
        inactiveDays: Int,
        lastActivityAt: String,
        commissionId: String,
    ) = AlimtalkTemplate(
        templateCode = "COMMINACTIVE",
        content =
            "[S클래스] 진행 중인 의뢰 확인을 요청드립니다\n\n" +
                "$teacherName 선생님, 안녕하세요.\n\n" +
                "$studentName 학생의 탐구 의뢰에 ${inactiveDays}일간 활동이 없습니다.\n\n" +
                "마지막 활동: $lastActivityAt\n\n" +
                "앱에서 의뢰 진행 상황을 확인해 주세요.\n\n" +
                "※ 본 메시지는 발신 전용으로, S클래스에서 자동 발송됩니다.",
        buttons = listOf(teacherButton(name = "의뢰 확인하기", commissionId = commissionId)),
    )
}
