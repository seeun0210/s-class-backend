package com.sclass.domain.domains.inquiryplan.domain

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

@Entity
@Table(
    name = "inquiry_plans",
    indexes = [
        Index(name = "idx_inquiry_plans_source", columnList = "source_type,source_ref_id"),
        Index(name = "idx_inquiry_plans_status", columnList = "status"),
        Index(name = "idx_inquiry_plans_requester", columnList = "requested_by_user_id"),
    ],
)
class InquiryPlan(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 30)
    val sourceType: InquiryPlanSourceType,

    // source_type에 따라 참조:
    //   COURSE_ROADMAP → Course.id
    //   LESSON         → Lesson.id
    @Column(name = "source_ref_id", nullable = false)
    val sourceRefId: Long,

    @Column(name = "requested_by_user_id", nullable = false, length = 26)
    val requestedByUserId: String,

    @Column(nullable = false, length = 200)
    val subject: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: InquiryPlanStatus = InquiryPlanStatus.PENDING,

    @Column(name = "external_plan_id", length = 100)
    var externalPlanId: String? = null,

    @Column(name = "failure_reason", length = 1000)
    var failureReason: String? = null,
) : BaseTimeEntity() {
    fun markReady(externalPlanId: String) {
        require(status == InquiryPlanStatus.PENDING) { "Cannot mark ready from $status" }
        this.externalPlanId = externalPlanId
        this.status = InquiryPlanStatus.READY
    }

    fun markFailed(reason: String) {
        require(status == InquiryPlanStatus.PENDING) { "Cannot mark failed from $status" }
        this.failureReason = reason
        this.status = InquiryPlanStatus.FAILED
    }
}
