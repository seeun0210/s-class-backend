package com.sclass.domain.domains.organization.domain

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
class OrganizationSettings(
    @Column(nullable = false)
    var useSupporters: Boolean = false,
)
