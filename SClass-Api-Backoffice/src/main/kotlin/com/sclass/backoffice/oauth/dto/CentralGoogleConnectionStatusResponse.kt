package com.sclass.backoffice.oauth.dto

import com.sclass.domain.domains.oauth.domain.CentralGoogleAccount
import java.time.LocalDateTime

data class CentralGoogleConnectionStatusResponse(
    val connected: Boolean,
    val googleEmail: String? = null,
    val allowedEmail: String? = null,
    val connectedByAdminUserId: String? = null,
    val connectedAt: LocalDateTime? = null,
    val lastUsedAt: LocalDateTime? = null,
    val scope: String? = null,
) {
    companion object {
        fun connected(
            account: CentralGoogleAccount,
            allowedEmail: String,
        ) = CentralGoogleConnectionStatusResponse(
            connected = true,
            googleEmail = account.googleEmail,
            allowedEmail = allowedEmail,
            connectedByAdminUserId = account.connectedByAdminUserId,
            connectedAt = account.connectedAt,
            lastUsedAt = account.lastUsedAt,
            scope = account.scope,
        )

        fun notConnected(allowedEmail: String? = null) =
            CentralGoogleConnectionStatusResponse(
                connected = false,
                allowedEmail = allowedEmail,
            )
    }
}
