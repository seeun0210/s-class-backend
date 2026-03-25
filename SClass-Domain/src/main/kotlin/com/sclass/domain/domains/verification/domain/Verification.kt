package com.sclass.domain.domains.verification.domain

import com.sclass.domain.common.model.BaseTimeEntity
import com.sclass.domain.common.vo.Ulid
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(
    name = "verifications",
    indexes = [
        Index(name = "idx_verifications_channel_target_created_at", columnList = "channel, target, createdAt"),
    ],
)
class Verification(
    @Id
    @Column(length = 26)
    val id: String = Ulid.generate(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    val channel: VerificationChannel,

    @Column(nullable = false)
    val target: String,

    @Column(nullable = false, length = 6)
    val code: String,

    @Column(nullable = false)
    val expiresAt: LocalDateTime,

    @Column(nullable = false)
    var attemptCount: Int = 0,

    @Column(nullable = false)
    var verified: Boolean = false,
) : BaseTimeEntity() {
    fun isExpired(): Boolean = LocalDateTime.now().isAfter(expiresAt)

    fun incrementAttemptCount() {
        attemptCount++
    }

    fun verify() {
        verified = true
    }

    companion object {
        private const val CODE_LENGTH = 6
        private const val EXPIRATION_MINUTES = 5L

        fun create(
            channel: VerificationChannel,
            target: String,
        ): Verification {
            val code = (0 until CODE_LENGTH).map { (0..9).random() }.joinToString("")
            return Verification(
                channel = channel,
                target = target,
                code = code,
                expiresAt = LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES),
            )
        }
    }
}
