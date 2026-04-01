package com.sclass.infrastructure.message

import com.sclass.infrastructure.message.dto.AlimtalkTemplate

object VerificationTemplate {
    fun verificationCode(code: String) =
        AlimtalkTemplate(
            templateCode = "sclassPhoneVerify",
            content = "[S-Class] 휴대전화 인증번호 안내\n\n인증번호는 $code 입니다. 5분 이내에 입력해주세요.\n\n본인이 요청하지 않았다면 이 메시지를 무시해주세요.",
        )
}
