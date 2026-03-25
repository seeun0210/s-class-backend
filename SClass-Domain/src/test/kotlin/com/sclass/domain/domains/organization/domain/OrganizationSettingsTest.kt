package com.sclass.domain.domains.organization.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class OrganizationSettingsTest {
    @Test
    fun `기본값은 모든 기능이 비활성화 상태이다`() {
        val settings = OrganizationSettings()

        assertFalse(settings.useSupporters)
        assertFalse(settings.useLms)
    }

    @Test
    fun `개별 기능을 독립적으로 활성화할 수 있다`() {
        val lmsOnly = OrganizationSettings(useLms = true)
        val supportersOnly = OrganizationSettings(useSupporters = true)

        assertTrue(lmsOnly.useLms)
        assertFalse(lmsOnly.useSupporters)

        assertTrue(supportersOnly.useSupporters)
        assertFalse(supportersOnly.useLms)
    }

    @Test
    fun `data class equality로 동일한 설정을 비교할 수 있다`() {
        val settings1 = OrganizationSettings(useSupporters = true, useLms = true)
        val settings2 = OrganizationSettings(useSupporters = true, useLms = true)

        assertEquals(settings1, settings2)
    }
}
