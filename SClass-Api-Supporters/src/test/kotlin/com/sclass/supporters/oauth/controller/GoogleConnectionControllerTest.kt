package com.sclass.supporters.oauth.controller

import com.sclass.domain.domains.user.domain.Role
import com.sclass.supporters.oauth.dto.GoogleOAuthStateResponse
import com.sclass.supporters.oauth.usecase.ConnectGoogleUseCase
import com.sclass.supporters.oauth.usecase.DisconnectGoogleUseCase
import com.sclass.supporters.oauth.usecase.GetGoogleConnectionStatusUseCase
import com.sclass.supporters.oauth.usecase.IssueGoogleOAuthStateUseCase
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping

class GoogleConnectionControllerTest {
    private lateinit var issueStateUseCase: IssueGoogleOAuthStateUseCase
    private lateinit var controller: GoogleConnectionController

    @BeforeEach
    fun setUp() {
        issueStateUseCase = mockk()
        controller =
            GoogleConnectionController(
                connectGoogleUseCase = mockk<ConnectGoogleUseCase>(),
                disconnectGoogleUseCase = mockk<DisconnectGoogleUseCase>(),
                getStatusUseCase = mockk<GetGoogleConnectionStatusUseCase>(),
                issueStateUseCase = issueStateUseCase,
            )
    }

    @Test
    fun `state 발급 endpoint는 POST로 노출된다`() {
        val method =
            GoogleConnectionController::class.java.getDeclaredMethod(
                "issueState",
                String::class.java,
                String::class.java,
            )
        val postMapping = method.getAnnotation(PostMapping::class.java)

        assertAll(
            { assertNotNull(postMapping) },
            { assertEquals("/state", postMapping.value.single()) },
            { assertNull(method.getAnnotation(GetMapping::class.java)) },
        )
    }

    @Test
    fun `state 발급 응답은 캐시를 금지한다`() {
        every { issueStateUseCase.execute("teacher-user-id", Role.TEACHER) } returns
            GoogleOAuthStateResponse(
                state = "state-abc",
                expiresInSeconds = 300,
            )

        val response = controller.issueState("teacher-user-id", Role.TEACHER.name)

        assertAll(
            { assertEquals(HttpStatus.OK, response.statusCode) },
            { assertEquals("no-store", response.headers.cacheControl) },
            { assertEquals(true, response.body?.success) },
            { assertEquals("state-abc", response.body?.data?.state) },
            { assertEquals(300L, response.body?.data?.expiresInSeconds) },
        )
    }
}
