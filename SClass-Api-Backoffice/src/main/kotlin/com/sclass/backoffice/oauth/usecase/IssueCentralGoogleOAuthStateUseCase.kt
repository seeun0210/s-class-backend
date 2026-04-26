package com.sclass.backoffice.oauth.usecase

import com.sclass.backoffice.oauth.dto.CentralGoogleOAuthStateResponse
import com.sclass.common.annotation.UseCase
import com.sclass.infrastructure.calendar.GoogleCentralCalendarProperties
import com.sclass.infrastructure.oauth.state.GoogleOAuthStateStore
import java.time.Duration

@UseCase
class IssueCentralGoogleOAuthStateUseCase(
    private val stateStore: GoogleOAuthStateStore,
    private val properties: GoogleCentralCalendarProperties,
) {
    fun execute(adminUserId: String): CentralGoogleOAuthStateResponse =
        CentralGoogleOAuthStateResponse(
            state = stateStore.issue(adminUserId, STATE_TTL),
            expiresInSeconds = STATE_TTL.seconds,
            clientId = properties.requireClientId(),
            scopes = CentralGoogleOAuthScopes.authorizationScopes,
        )

    companion object {
        private val STATE_TTL = Duration.ofMinutes(5)
    }
}
