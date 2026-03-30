package com.sclass.supporters.commission.dto

import com.sclass.domain.domains.commission.domain.ActivityType
import com.sclass.domain.domains.commission.domain.Commission
import com.sclass.domain.domains.commission.domain.CommissionStatus
import com.sclass.domain.domains.commission.domain.OutputFormat
import java.time.LocalDateTime

data class CommissionListResponse(
    val commissions: List<CommissionSummary>,
) {
    companion object {
        fun from(commissions: List<Commission>): CommissionListResponse =
            CommissionListResponse(
                commissions = commissions.map { CommissionSummary.from(it) },
            )
    }
}

data class CommissionSummary(
    val id: Long,
    val outputFormat: OutputFormat,
    val activityType: ActivityType,
    val status: CommissionStatus,
    val subject: String,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(commission: Commission): CommissionSummary =
            CommissionSummary(
                id = commission.id,
                outputFormat = commission.outputFormat,
                activityType = commission.activityType,
                status = commission.status,
                subject = commission.guideInfo.subject,
                createdAt = commission.createdAt,
            )
    }
}
