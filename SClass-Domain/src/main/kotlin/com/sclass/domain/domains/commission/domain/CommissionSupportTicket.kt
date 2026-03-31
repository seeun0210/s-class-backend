package com.sclass.domain.domains.commission.domain

import com.sclass.domain.common.model.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "commission_support_tickets")
class CommissionSupportTicket(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commission_id", nullable = false)
    val commission: Commission,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    val type: SupportTicketType,

    @Column(nullable = false, columnDefinition = "TEXT")
    val reason: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: TicketStatus = TicketStatus.OPEN,
) : BaseTimeEntity() {
    fun resolve() {
        this.status = TicketStatus.RESOLVED
    }
}
