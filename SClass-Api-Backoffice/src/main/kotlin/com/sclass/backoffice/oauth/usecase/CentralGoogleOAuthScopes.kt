package com.sclass.backoffice.oauth.usecase

object CentralGoogleOAuthScopes {
    const val GOOGLE_CALENDAR_EVENTS_SCOPE = "https://www.googleapis.com/auth/calendar.events"
    const val GOOGLE_DRIVE_MEET_READONLY_SCOPE = "https://www.googleapis.com/auth/drive.meet.readonly"

    val authorizationScopes =
        listOf(
            "openid",
            "https://www.googleapis.com/auth/userinfo.email",
            GOOGLE_CALENDAR_EVENTS_SCOPE,
            GOOGLE_DRIVE_MEET_READONLY_SCOPE,
        )

    val googleEmailScopes =
        setOf(
            "email",
            "https://www.googleapis.com/auth/userinfo.email",
        )
}
