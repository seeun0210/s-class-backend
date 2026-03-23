package com.sclass.infrastructure.email

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = ["email.smtp.enabled"], havingValue = "true")
class SmtpEmailSender(
    private val mailSender: JavaMailSender,
) : EmailSender {
    override fun sendVerificationCode(
        email: String,
        code: String,
    ) {
        val message = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, false, "UTF-8")
        helper.setTo(email)
        helper.setSubject("[S-Class] 이메일 인증번호: $code")
        helper.setText("인증번호: $code\n\n5분 이내에 입력해주세요.")
        mailSender.send(message)
    }
}
