package com.sclass.backoffice.oauth.usecase

import com.sclass.domain.domains.oauth.domain.CentralGoogleAccount
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class DisconnectCentralGoogleUseCaseTest {
    private val disconnectCentralGoogleAccountLockedUseCase: DisconnectCentralGoogleAccountLockedUseCase = mockk()
    private val useCase =
        DisconnectCentralGoogleUseCase(
            disconnectCentralGoogleAccountLockedUseCase = disconnectCentralGoogleAccountLockedUseCase,
        )

    @Test
    fun `중앙 Google provider key로 locked usecase에 위임한다`() {
        every { disconnectCentralGoogleAccountLockedUseCase.execute(CentralGoogleAccount.PROVIDER_GOOGLE) } just Runs

        useCase.execute()

        verify { disconnectCentralGoogleAccountLockedUseCase.execute(CentralGoogleAccount.PROVIDER_GOOGLE) }
    }
}
