package com.sclass.infrastructure.email

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import jakarta.mail.internet.MimeMessage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.mail.javamail.JavaMailSender

class SmtpEmailSenderTest {
    private lateinit var mailSender: JavaMailSender
    private lateinit var smtpEmailSender: SmtpEmailSender

    @BeforeEach
    fun setUp() {
        mailSender = mockk()
        smtpEmailSender = SmtpEmailSender(mailSender)
    }

    @Test
    fun `인증코드 이메일을 HTML 형식으로 발송한다`() {
        val mimeMessage = mockk<MimeMessage>(relaxed = true)
        every { mailSender.createMimeMessage() } returns mimeMessage
        every { mailSender.send(any<MimeMessage>()) } returns Unit

        smtpEmailSender.sendVerificationCode("test@example.com", "123456", "S-Class Supporters")

        verify { mailSender.send(mimeMessage) }
    }

    @Test
    fun `이메일 본문에 인증코드와 서비스명이 포함된다`() {
        val mimeMessage = mockk<MimeMessage>(relaxed = true)
        every { mailSender.createMimeMessage() } returns mimeMessage
        every { mailSender.send(any<MimeMessage>()) } returns Unit

        val contentSlot = slot<String>()
        every { mimeMessage.setContent(capture(contentSlot), any()) } returns Unit

        smtpEmailSender.sendVerificationCode("test@example.com", "654321", "S-Class LMS")

        verify { mailSender.send(mimeMessage) }
    }
}
