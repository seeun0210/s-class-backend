package com.sclass.domain.domains.user.domain

import com.sclass.domain.common.model.BaseTimeEntity
import com.sclass.domain.common.vo.Ulid
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "users")
class User(
    @Id
    @Column(length = 26)
    val id: String = Ulid.generate(),

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false)
    var name: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val authProvider: AuthProvider,

    var hashedPassword: String? = null,

    var oauthId: String? = null,

    @Column(nullable = false)
    var phoneNumber: String,

    var profileImageUrl: String? = null,

    @Column(nullable = false)
    var activated: Boolean = true,
) : BaseTimeEntity() {
    fun changePassword(newHashedPassword: String) {
        this.hashedPassword = newHashedPassword
    }

    companion object {
        private val PHONE_DIGITS_REGEX = Regex("^\\d{10,11}$")
        private val PHONE_FORMATTED_REGEX = Regex("^\\d{3}-\\d{3,4}-\\d{4}$")

        fun formatPhoneNumber(raw: String): String {
            val digits = raw.replace("-", "")
            if (!PHONE_DIGITS_REGEX.matches(digits)) {
                throw IllegalArgumentException("전화번호는 10~11자리 숫자여야 합니다")
            }
            return when (digits.length) {
                10 -> "${digits.substring(0, 3)}-${digits.substring(3, 6)}-${digits.substring(6)}"
                11 -> "${digits.substring(0, 3)}-${digits.substring(3, 7)}-${digits.substring(7)}"
                else -> throw IllegalArgumentException("전화번호는 10~11자리 숫자여야 합니다")
            }
        }
    }
}
