package com.sclass.domain.domains.oauth.domain

import com.sclass.domain.common.model.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(
    name = "central_google_accounts",
    indexes = [
        Index(name = "idx_central_google_email", columnList = "google_email"),
    ],
)
class CentralGoogleAccount(
    @Id
    @Column(length = 20)
    val provider: String = PROVIDER_GOOGLE,
    @Column(name = "google_email", nullable = false, length = 320)
    var googleEmail: String,
    @Column(name = "encrypted_refresh_token", nullable = false, columnDefinition = "TEXT")
    var encryptedRefreshToken: String,
    @Column(nullable = false, length = 500)
    var scope: String,
    @Column(name = "connected_by_admin_user_id", nullable = false, length = 26)
    var connectedByAdminUserId: String,
    @Column(name = "connected_at", nullable = false)
    var connectedAt: LocalDateTime,
    @Column(name = "last_used_at")
    var lastUsedAt: LocalDateTime? = null,
) : BaseTimeEntity() {
    fun reconnect(
        newGoogleEmail: String,
        newEncryptedRefreshToken: String,
        newScope: String,
        adminUserId: String,
        at: LocalDateTime,
    ) {
        this.googleEmail = newGoogleEmail
        this.encryptedRefreshToken = newEncryptedRefreshToken
        this.scope = newScope
        this.connectedByAdminUserId = adminUserId
        this.connectedAt = at
    }

    fun markUsed(at: LocalDateTime) {
        this.lastUsedAt = at
    }

    companion object {
        const val PROVIDER_GOOGLE = "GOOGLE"
    }
}
