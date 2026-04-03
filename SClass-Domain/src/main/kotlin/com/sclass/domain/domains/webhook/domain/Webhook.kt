package com.sclass.domain.domains.webhook.domain

import com.sclass.domain.common.model.BaseTimeEntity
import com.sclass.domain.common.vo.Ulid
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.security.SecureRandom

@Entity
@Table(name = "webhooks")
class Webhook(
    @Id
    @Column(length = 26)
    val id: String = Ulid.generate(),

    @Column(nullable = false)
    val name: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: WebhookType,

    @Column(nullable = false, unique = true)
    val secret: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: WebhookStatus = WebhookStatus.ACTIVE,

    @Embedded
    val fieldMapping: WebhookFieldMapping,
) : BaseTimeEntity() {
    fun toggleStatus() {
        status =
            if (status == WebhookStatus.ACTIVE) {
                WebhookStatus.INACTIVE
            } else {
                WebhookStatus.ACTIVE
            }
    }

    companion object {
        fun create(
            name: String,
            type: WebhookType,
            fieldMapping: WebhookFieldMapping,
        ): Webhook {
            val secret = generateSecret()
            return Webhook(
                name = name,
                type = type,
                secret = secret,
                fieldMapping =
                fieldMapping,
            )
        }

        private fun generateSecret(): String {
            val bytes = ByteArray(32)
            SecureRandom().nextBytes(bytes)
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }
}
