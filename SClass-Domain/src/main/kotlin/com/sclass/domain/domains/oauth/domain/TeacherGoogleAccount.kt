package com.sclass.domain.domains.oauth.domain

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
    name = "teacher_google_accounts",
    indexes = [
        Index(name = "uq_teacher_google_user_id", columnList = "user_id", unique = true),
        Index(name = "idx_teacher_google_email", columnList = "google_email"),
    ],
)
class TeacherGoogleAccount(
    @Id
    @Column(length = 26)
    val id: String = Ulid.generate(),
    @Column(name = "user_id", nullable = false, unique = true, length = 26)
    val userId: String,
    @Column(name = "google_email", nullable = false, length = 320)
    var googleEmail: String,
    @Column(name = "encrypted_refresh_token", nullable = false, columnDefinition = "TEXT")
    var encryptedRefreshToken: String,
    @Column(nullable = false, length = 500)
    var scope: String,
    @Column(name = "connected_at", nullable = false)
    val connectedAt: LocalDateTime,
    @Column(name = "last_used_at")
    var lastUsedAt: LocalDateTime? = null,
) : BaseTimeEntity() {
    fun reconnect(
        newGoogleEmail: String,
        newEncryptedRefreshToken: String,
        newScope: String,
    ) {
        this.googleEmail = newGoogleEmail
        this.encryptedRefreshToken = newEncryptedRefreshToken
        this.scope = newScope
    }

    fun markUsed(at: LocalDateTime) {
        this.lastUsedAt = at
    }
}
