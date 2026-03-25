package com.sclass.domain.domains.organization.domain

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class OrganizationSettings(
    @Column(nullable = false)
    val useSupporters: Boolean = false,

    @Column(nullable = false)
    val useLms: Boolean = false,
)
