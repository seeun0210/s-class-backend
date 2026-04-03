package com.sclass.infrastructure.message

import com.sclass.infrastructure.message.dto.AlimtalkRequest
import com.sclass.infrastructure.message.dto.AlimtalkTemplate

class DiagnosisAlimtalkTemplates {
    fun surveySubmitted(
        studentName: String,
        submittedAt: String,
    ) = AlimtalkTemplate(
        templateCode = "SURVEYSUBMITTED",
        content =
            "안녕하세요, $studentName 님!\n\n" +
                "제출하신 입시 진단 설문이 정상적으로 접수되었습니다.\n\n" +
                "■ 제출 일시: $submittedAt\n\n" +
                "진단이 완료되면 결과 확인 링크를 알림톡으로 보내드리겠습니다.",
    )

    fun surveySubmittedParent(studentName: String) =
        AlimtalkTemplate(
            templateCode = "SURVEYSUBMITTEDPARENT",
            content =
                "안녕하세요, $studentName 학생 학부모님!\n\n" +
                    "자녀의 진단 설문이 정상적으로 접수되었습니다.\n\n" +
                    "진단이 완료되면 결과 확인 링크를 알림톡으로 보내드리겠습니다.",
        )

    fun diagnosisCompleted(
        studentName: String,
        resultUrl: String,
    ) = AlimtalkTemplate(
        templateCode = "DIAGCOMPLETED", // NCP 템플릿 등록 필요
        content =
            "[S클래스] 진단 리포트가 완성되었습니다\n\n" +
                "$studentName 학생의 진단 분석이 완료되었습니다.\n\n" +
                "아래 링크에서 상세한 진단 리포트를 확인하세요.\n\n" +
                "※ 본 메시지는 발신 전용으로, S클래스에서 자동 발송됩니다.",
        buttons =
            listOf(
                AlimtalkRequest.Button(
                    name = "리포트 확인하기",
                    linkMobile = resultUrl,
                    linkPc = resultUrl,
                ),
            ),
    )
}
