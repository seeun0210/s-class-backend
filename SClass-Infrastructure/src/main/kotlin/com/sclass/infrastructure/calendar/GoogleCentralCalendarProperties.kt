package com.sclass.infrastructure.calendar

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "google.calendar.central")
class GoogleCentralCalendarProperties {
    var enabled: Boolean = false
    var calendarId: String = "primary"
    var allowedEmail: String = ""

    fun requireAllowedEmail(): String =
        allowedEmail.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("google.calendar.central.allowed-email is required")
}
