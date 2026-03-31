package com.sclass.domain.domains.commission.domain

import com.sclass.domain.common.model.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "commissions")
class Commission(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "student_user_id", nullable = false, length = 26)
    val studentUserId: String,

    @Column(name = "teacher_user_id", nullable = false, length = 26)
    val teacherUserId: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    val outputFormat: OutputFormat,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    val activityType: ActivityType,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var status: CommissionStatus = CommissionStatus.REQUESTED,

    @Embedded
    val guideInfo: GuideInfo,

    @Column(name = "selected_topic_id")
    var selectedTopicId: Long? = null,
) : BaseTimeEntity() {
    fun requestAdditionalInfo() {
        validateStatusTransition(CommissionStatus.ADDITIONAL_INFO_REQUESTED)
        this.status = CommissionStatus.ADDITIONAL_INFO_REQUESTED
    }

    fun resubmit() {
        validateStatusTransition(CommissionStatus.REQUESTED)
        this.status = CommissionStatus.REQUESTED
    }

    fun proposeTopics() {
        validateStatusTransition(CommissionStatus.TOPIC_PROPOSED)
        this.status = CommissionStatus.TOPIC_PROPOSED
    }

    fun selectTopic() {
        validateStatusTransition(CommissionStatus.TOPIC_SELECTED)
        this.status = CommissionStatus.TOPIC_SELECTED
    }

    fun start() {
        validateStatusTransition(CommissionStatus.IN_PROGRESS)
        this.status = CommissionStatus.IN_PROGRESS
    }

    fun complete() {
        validateStatusTransition(CommissionStatus.COMPLETED)
        this.status = CommissionStatus.COMPLETED
    }

    fun reject(reason: String) {
        validateStatusTransition(CommissionStatus.REJECTED)
        this.status = CommissionStatus.REJECTED
    }

    fun cancel() {
        validateStatusTransition(CommissionStatus.CANCELLED)
        this.status = CommissionStatus.CANCELLED
    }

    private fun validateStatusTransition(target: CommissionStatus) {
        val allowed =
            when (target) {
                CommissionStatus.REQUESTED -> setOf(CommissionStatus.ADDITIONAL_INFO_REQUESTED, CommissionStatus.REQUESTED)
                CommissionStatus.ADDITIONAL_INFO_REQUESTED -> setOf(CommissionStatus.REQUESTED, CommissionStatus.ADDITIONAL_INFO_REQUESTED)
                CommissionStatus.TOPIC_PROPOSED -> setOf(CommissionStatus.REQUESTED)
                CommissionStatus.TOPIC_SELECTED -> setOf(CommissionStatus.TOPIC_PROPOSED)
                CommissionStatus.IN_PROGRESS -> setOf(CommissionStatus.TOPIC_SELECTED)
                CommissionStatus.COMPLETED -> setOf(CommissionStatus.IN_PROGRESS)
                CommissionStatus.REJECTED ->
                    setOf(
                        CommissionStatus.REQUESTED,
                        CommissionStatus.ADDITIONAL_INFO_REQUESTED,
                    )
                CommissionStatus.CANCELLED ->
                    setOf(
                        CommissionStatus.REQUESTED,
                        CommissionStatus.ADDITIONAL_INFO_REQUESTED,
                        CommissionStatus.TOPIC_PROPOSED,
                        CommissionStatus.TOPIC_SELECTED,
                    )
            }
        require(status in allowed) {
            "Cannot transition from $status to $target"
        }
    }
}
