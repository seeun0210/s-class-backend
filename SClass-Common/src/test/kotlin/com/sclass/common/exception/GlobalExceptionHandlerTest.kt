package com.sclass.common.exception

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpInputMessage
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import java.io.InputStream

class GlobalExceptionHandlerTest {
    private lateinit var handler: GlobalExceptionHandler

    @BeforeEach
    fun setUp() {
        handler = GlobalExceptionHandler()
    }

    @Test
    fun `BusinessException 처리 시 올바른 status와 code를 반환한다`() {
        val errorCode =
            object : ErrorCode {
                override val code = "TEST_001"
                override val message = "테스트 에러"
                override val httpStatus = 409
            }
        val exception = BusinessException(errorCode)

        val response = handler.handleBusinessException(exception)

        assertEquals(409, response.statusCode.value())
        assertEquals("TEST_001", response.body?.error?.code)
    }

    @Test
    fun `BusinessException 처리 시 success는 false이다`() {
        val errorCode =
            object : ErrorCode {
                override val code = "TEST_002"
                override val message = "테스트"
                override val httpStatus = 404
            }
        val exception = BusinessException(errorCode)

        val response = handler.handleBusinessException(exception)

        assertFalse(response.body!!.success)
    }

    @Test
    fun `MethodArgumentNotValidException 처리 시 400과 필드에러 메시지를 반환한다`() {
        val bindException = BindException(Any(), "target")
        bindException.addError(FieldError("target", "email", "must not be blank"))
        val exception =
            MethodArgumentNotValidException(
                org.springframework.core.MethodParameter(
                    GlobalExceptionHandler::class.java.getDeclaredMethod(
                        "handleValidation",
                        MethodArgumentNotValidException::class.java,
                    ),
                    0,
                ),
                bindException.bindingResult,
            )

        val response = handler.handleValidation(exception)

        assertEquals(400, response.statusCode.value())
        assertEquals("GLOBAL_001", response.body?.error?.code)
        val message =
            response.body
                ?.error
                ?.message
                .orEmpty()
        assertTrue(message.contains("email"))
    }

    @Test
    fun `HttpMessageNotReadableException 처리 시 400과 INVALID_INPUT 코드를 반환한다`() {
        val httpInputMessage =
            object : HttpInputMessage {
                override fun getBody(): InputStream = InputStream.nullInputStream()

                override fun getHeaders(): HttpHeaders = HttpHeaders()
            }
        val exception = HttpMessageNotReadableException("Invalid JSON", httpInputMessage)

        val response = handler.handleHttpMessageNotReadable(exception)

        assertEquals(400, response.statusCode.value())
        assertEquals("GLOBAL_001", response.body?.error?.code)
    }

    @Test
    fun `처리되지 않은 예외 발생 시 500과 INTERNAL_ERROR 코드를 반환한다`() {
        val exception = RuntimeException("unexpected error")

        val response = handler.handleException(exception)

        assertEquals(500, response.statusCode.value())
        assertEquals("GLOBAL_500", response.body?.error?.code)
    }
}
