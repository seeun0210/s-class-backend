package com.sclass.domain.domains.partnership.domain

import com.sclass.domain.common.model.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "partnership_leads",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_partnership_leads_phone", columnNames = ["phone"]),
    ],
    indexes = [
        Index(name = "idx_partnership_leads_status", columnList = "status"),
        Index(name = "idx_partnership_leads_created_at", columnList = "created_at"),
    ],
)
class PartnershipLead(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(nullable = false, length = 100)
    val academyName: String,

    @Column(nullable = false, length = 20)
    val phone: String,

    @Column(length = 255)
    val email: String?,

    @Column(columnDefinition = "TEXT")
    val message: String?,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: PartnershipLeadStatus = PartnershipLeadStatus.NEW,

    @Column(columnDefinition = "TEXT")
    var note: String? = null,
) : BaseTimeEntity() {
    fun updateStatus(
        status: PartnershipLeadStatus,
        note: String?,
    ) {
        this.status = status
        if (note != null) this.note = note
    }
}
