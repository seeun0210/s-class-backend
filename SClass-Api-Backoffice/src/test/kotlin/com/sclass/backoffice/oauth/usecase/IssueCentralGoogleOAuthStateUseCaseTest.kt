package com.sclass.backoffice.oauth.usecase

import com.sclass.infrastructure.calendar.GoogleCentralCalendarProperties
import com.sclass.infrastructure.oauth.state.GoogleOAuthStateStore
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class IssueCentralGoogleOAuthStateUseCaseTest {
    private val stateStore: GoogleOAuthStateStore = mockk()
    private val properties =
        GoogleCentralCalendarProperties().apply {
            clientId = "central-client-id"
        }
    private val useCase =
        IssueCentralGoogleOAuthStateUseCase(
            stateStore = stateStore,
            properties = properties,
        )

    @Test
    fun `중앙 Google OAuth 연결에 필요한 client id와 scope를 함께 반환한다`() {
        every { stateStore.issue("admin-user-id-0000000001", any()) } returns "issued-state"

        val response = useCase.execute("admin-user-id-0000000001")

        assertEquals("issued-state", response.state)
        assertEquals(300, response.expiresInSeconds)
        assertEquals("central-client-id", response.clientId)
        assertEquals(CentralGoogleOAuthScopes.authorizationScopes, response.scopes)
    }
}
