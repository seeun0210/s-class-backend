package com.sclass.domain.domains.organization.domain

import com.sclass.domain.common.model.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
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
    val domain: String,

    @Column(length = 500)
    var logoUrl: String? = null,

    @Column(unique = true, length = 6)
    val inviteCode: String? = null,

    @Embedded
    var settings: OrganizationSettings = OrganizationSettings(),

) : BaseTimeEntity() {
    fun changeName(newName: String) {
        this.name = newName
    }

    fun changeLogoUrl(newLogoUrl: String?) {
        this.logoUrl = newLogoUrl
    }

    fun changeSettings(newSettings: OrganizationSettings) {
        this.settings = newSettings
    }
}
