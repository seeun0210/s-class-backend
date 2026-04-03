package com.sclass.infrastructure.message

import com.sclass.infrastructure.message.dto.AlimtalkRequest
import com.sclass.infrastructure.message.dto.AlimtalkTemplate

class DiagnosisAlimtalkTemplates {
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
