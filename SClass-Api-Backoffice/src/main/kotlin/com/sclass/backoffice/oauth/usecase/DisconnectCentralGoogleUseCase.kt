package com.sclass.backoffice.oauth.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.oauth.domain.CentralGoogleAccount

@UseCase
class DisconnectCentralGoogleUseCase(
    private val disconnectCentralGoogleAccountLockedUseCase: DisconnectCentralGoogleAccountLockedUseCase,
) {
    fun execute() = disconnectCentralGoogleAccountLockedUseCase.execute(CentralGoogleAccount.PROVIDER_GOOGLE)
}
