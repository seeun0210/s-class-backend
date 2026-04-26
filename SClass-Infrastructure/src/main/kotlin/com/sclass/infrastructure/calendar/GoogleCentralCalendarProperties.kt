package com.sclass.infrastructure.calendar

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "google.calendar.central")
class GoogleCentralCalendarProperties {
    var enabled: Boolean = false
    var calendarId: String = "primary"
    var allowedEmail: String = ""
    var clientId: String = ""
    var clientSecret: String = ""

    fun requireAllowedEmail(): String =
        allowedEmail.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("google.calendar.central.allowed-email is required")

    fun requireClientId(): String =
        clientId.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("google.calendar.central.client-id is required")

    fun requireClientSecret(): String =
        clientSecret.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("google.calendar.central.client-secret is required")
}
