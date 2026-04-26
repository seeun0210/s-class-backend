package com.sclass.infrastructure.calendar

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GoogleCentralCalendarPropertiesTest {
    @Test
    fun `allowed email이 있으면 반환한다`() {
        val properties =
            GoogleCentralCalendarProperties().apply {
                allowedEmail = "central-google@example.com"
            }

        assertEquals("central-google@example.com", properties.requireAllowedEmail())
    }

    @Test
    fun `allowed email이 없으면 예외`() {
        assertThrows<IllegalStateException> {
            GoogleCentralCalendarProperties().requireAllowedEmail()
        }
    }

    @Test
    fun `central client id와 secret이 있으면 반환한다`() {
        val properties =
            GoogleCentralCalendarProperties().apply {
                clientId = "central-client-id"
                clientSecret = "central-client-secret"
            }

        assertEquals("central-client-id", properties.requireClientId())
        assertEquals("central-client-secret", properties.requireClientSecret())
    }

    @Test
    fun `central client id와 secret이 없으면 예외`() {
        val properties = GoogleCentralCalendarProperties()

        assertThrows<IllegalStateException> {
            properties.requireClientId()
        }
        assertThrows<IllegalStateException> {
            properties.requireClientSecret()
        }
    }
}
