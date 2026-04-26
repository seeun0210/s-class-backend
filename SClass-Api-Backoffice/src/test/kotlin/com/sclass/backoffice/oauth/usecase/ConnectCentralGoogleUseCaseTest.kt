package com.sclass.backoffice.oauth.usecase

import com.sclass.backoffice.oauth.dto.CentralGoogleConnectionStatusResponse
import com.sclass.backoffice.oauth.dto.ConnectCentralGoogleRequest
import com.sclass.domain.domains.oauth.domain.CentralGoogleAccount
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class ConnectCentralGoogleUseCaseTest {
    private val connectCentralGoogleLockedUseCase: ConnectCentralGoogleLockedUseCase = mockk()
    private val useCase =
        ConnectCentralGoogleUseCase(
            connectCentralGoogleLockedUseCase = connectCentralGoogleLockedUseCase,
        )

    @Test
    fun `중앙 Google provider key로 locked usecase에 위임한다`() {
        val request = ConnectCentralGoogleRequest(code = "auth-code", redirectUri = "https://backoffice/callback", state = "state")
        val expected = CentralGoogleConnectionStatusResponse.notConnected("central-google@example.com")
        every {
            connectCentralGoogleLockedUseCase.execute(
                provider = CentralGoogleAccount.PROVIDER_GOOGLE,
                adminUserId = "admin-user-id-0000000001",
                request = request,
            )
        } returns expected

        val response = useCase.execute("admin-user-id-0000000001", request)

        assertSame(expected, response)
        verify {
            connectCentralGoogleLockedUseCase.execute(
                provider = CentralGoogleAccount.PROVIDER_GOOGLE,
                adminUserId = "admin-user-id-0000000001",
                request = request,
            )
        }
    }
}
