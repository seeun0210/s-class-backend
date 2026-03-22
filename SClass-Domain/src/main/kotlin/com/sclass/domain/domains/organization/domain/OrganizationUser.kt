package com.sclass.domain.domains.organization.domain

import com.sclass.domain.common.model.BaseTimeEntity
import com.sclass.domain.common.vo.Ulid
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "organization_users",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "organization_id"])],
)
class OrganizationUser(
    @Id
    @Column(length = 26)
    val id: String = Ulid.generate(),

    @Column(name = "user_id", nullable = false, length = 26)
    val userId: String,

    @Column(name = "organization_id", nullable = false)
    val organizationId: Long,
) : BaseTimeEntity()
