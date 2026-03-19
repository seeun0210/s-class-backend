package com.sclass.domain.domains.user.domain

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
    name = "user_roles",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["userId", "platform", "role"]),
    ],
)
class UserRole(
    @Id
    @Column(length = 26)
    val id: String = Ulid.generate(),

    @Column(nullable = false, length = 26)
    val userId: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val platform: Platform,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: Role,
) : BaseTimeEntity()
