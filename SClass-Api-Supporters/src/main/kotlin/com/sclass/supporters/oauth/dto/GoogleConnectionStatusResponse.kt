package com.sclass.supporters.oauth.dto

import com.sclass.domain.domains.oauth.domain.TeacherGoogleAccount
import java.time.LocalDateTime

data class GoogleConnectionStatusResponse(
    val connected: Boolean,
    val googleEmail: String? = null,
    val connectedAt: LocalDateTime? = null,
    val scope: String? = null,
) {
    companion object {
        fun connected(account: TeacherGoogleAccount) =
            GoogleConnectionStatusResponse(
                connected = true,
                googleEmail = account.googleEmail,
                connectedAt = account.connectedAt,
                scope = account.scope,
            )

        fun notConnected() = GoogleConnectionStatusResponse(connected = false)
    }
}
