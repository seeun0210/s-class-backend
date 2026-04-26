package com.sclass.backoffice.oauth.usecase

import com.sclass.backoffice.oauth.dto.CentralGoogleConnectionStatusResponse
import com.sclass.backoffice.oauth.dto.ConnectCentralGoogleRequest
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.oauth.domain.CentralGoogleAccount

@UseCase
class ConnectCentralGoogleUseCase(
    private val connectCentralGoogleLockedUseCase: ConnectCentralGoogleLockedUseCase,
) {
    fun execute(
        adminUserId: String,
        request: ConnectCentralGoogleRequest,
    ): CentralGoogleConnectionStatusResponse =
        connectCentralGoogleLockedUseCase.execute(
            provider = CentralGoogleAccount.PROVIDER_GOOGLE,
            adminUserId = adminUserId,
            request = request,
        )
}
