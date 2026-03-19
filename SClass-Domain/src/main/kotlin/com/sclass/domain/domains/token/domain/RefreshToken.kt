package com.sclass.domain.domains.token.domain

import com.sclass.domain.common.model.BaseTimeEntity
import com.sclass.domain.common.vo.Ulid
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(
    name = "refresh_tokens",
    indexes = [
        Index(name = "idx_refresh_token_user_id", columnList = "userId"),
    ],
)
class RefreshToken(
    @Id
    @Column(length = 26)
    val id: String = Ulid.generate(),

    @Column(nullable = false, length = 26)
    val userId: String,

    @Column(nullable = false)
    val expiresAt: LocalDateTime,
) : BaseTimeEntity() {
    fun isValid(): Boolean = expiresAt.isAfter(LocalDateTime.now())
}
