package com.sclass.domain.domains.organization.domain

import com.sclass.domain.common.model.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "organizations")
class Organization(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(nullable = false, length = 200)
    var name: String,

    @Column(nullable = false, unique = true, length = 200)
    var domain: String,

    @Column(length = 500)
    var logoUrl: String? = null,

    @Column(unique = true, length = 6)
    var inviteCode: String? = null,

    @Embedded
    var settings: OrganizationSettings = OrganizationSettings(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OrganizationStatus = OrganizationStatus.ACTIVE,
) : BaseTimeEntity()
