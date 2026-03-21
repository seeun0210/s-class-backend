package com.sclass.domain.domains.organization.domain

import com.sclass.domain.common.model.BaseTimeEntity
import com.sclass.domain.common.vo.Ulid
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "organization_attributions",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["organizationId", "userId"]),
    ],
)
class OrganizationAttribution(
    @Id
    @Column(length = 26)
    val id: String = Ulid.generate(),

    @Column(nullable = false)
    val organizationId: Long,

    @Column(nullable = false, unique = true, length = 26)
    val userId: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val source: AttributionSource,

    var originService: String? = null,
) : BaseTimeEntity()
