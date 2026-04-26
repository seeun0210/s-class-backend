package com.sclass.supporters.oauth.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.oauth.adaptor.TeacherGoogleAccountAdaptor
import com.sclass.supporters.oauth.dto.GoogleConnectionStatusResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetGoogleConnectionStatusUseCase(
    private val accountAdaptor: TeacherGoogleAccountAdaptor,
) {
    @Transactional
    fun execute(userId: String): GoogleConnectionStatusResponse {
        val account = accountAdaptor.findByUserIdOrNull(userId)

        return if (account != null) {
            GoogleConnectionStatusResponse.connected(account)
        } else {
            GoogleConnectionStatusResponse.notConnected()
        }
    }
}
