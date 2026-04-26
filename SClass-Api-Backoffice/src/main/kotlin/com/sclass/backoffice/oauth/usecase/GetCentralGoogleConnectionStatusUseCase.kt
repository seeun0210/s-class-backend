package com.sclass.backoffice.oauth.usecase

import com.sclass.backoffice.oauth.dto.CentralGoogleConnectionStatusResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.oauth.adaptor.CentralGoogleAccountAdaptor
import com.sclass.infrastructure.calendar.GoogleCentralCalendarProperties
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetCentralGoogleConnectionStatusUseCase(
    private val centralGoogleAccountAdaptor: CentralGoogleAccountAdaptor,
    private val properties: GoogleCentralCalendarProperties,
) {
    @Transactional(readOnly = true)
    fun execute(): CentralGoogleConnectionStatusResponse {
        val allowedEmail = properties.allowedEmail.takeIf { it.isNotBlank() }
        val account = centralGoogleAccountAdaptor.findGoogleOrNull()

        return account
            ?.let { CentralGoogleConnectionStatusResponse.connected(it, allowedEmail.orEmpty()) }
            ?: CentralGoogleConnectionStatusResponse.notConnected(allowedEmail)
    }
}
