package com.sclass.supporters.oauth.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.common.exception.ForbiddenException
import com.sclass.domain.domains.user.domain.Role
import com.sclass.infrastructure.oauth.state.GoogleOAuthStateStore
import com.sclass.supporters.oauth.dto.GoogleOAuthStateResponse
import java.time.Duration

@UseCase
class IssueGoogleOAuthStateUseCase(
    private val stateStore: GoogleOAuthStateStore,
) {
    fun execute(
        userId: String,
        role: Role,
    ): GoogleOAuthStateResponse {
        if (role != Role.TEACHER) throw ForbiddenException()

        return GoogleOAuthStateResponse(
            state = stateStore.issue(userId, STATE_TTL),
            expiresInSeconds = STATE_TTL.seconds,
        )
    }

    companion object {
        private val STATE_TTL = Duration.ofMinutes(5)
    }
}
