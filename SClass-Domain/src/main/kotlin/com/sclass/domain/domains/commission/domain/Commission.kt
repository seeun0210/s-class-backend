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
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.Version

@Entity
@Table(
    name = "commissions",
    indexes = [
        Index(name = "idx_commissions_student", columnList = "student_user_id"),
        Index(name = "idx_commissions_teacher", columnList = "teacher_user_id"),
        Index(name = "idx_commissions_product", columnList = "product_id"),
        Index(name = "idx_commissions_status", columnList = "status"),
    ],
)
class Commission(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "student_user_id", nullable = false, length = 26)
    val studentUserId: String,

    @Column(name = "teacher_user_id", nullable = false, length = 26)
    val teacherUserId: String,

    // 요청 시점의 CommissionProduct 스냅샷 참조
    @Column(name = "product_id", nullable = false, length = 26)
    val productId: String,

    // 요청 시점의 교사 보수 스냅샷 (TeacherPayoutResolver 결과를 굳혀둠)
    @Column(name = "teacher_payout_amount_won", nullable = false)
    val teacherPayoutAmountWon: Int,

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

    // 수락 시 생성된 Lesson FK
    @Column(name = "accepted_lesson_id")
    var acceptedLessonId: Long? = null,

    @Version
    var version: Long = 0,
) : BaseTimeEntity() {
    fun proposeTopics() = transitionTo(CommissionStatus.TOPIC_PROPOSED)

    fun selectTopicAndAccept(
        topicId: Long,
        lessonId: Long,
    ) {
        transitionTo(CommissionStatus.ACCEPTED)
        this.selectedTopicId = topicId
        this.acceptedLessonId = lessonId
    }

    fun reject() = transitionTo(CommissionStatus.REJECTED)

    fun cancel() = transitionTo(CommissionStatus.CANCELLED)

    private fun transitionTo(target: CommissionStatus) {
        val allowed =
            when (target) {
                CommissionStatus.REQUESTED -> emptySet()
                CommissionStatus.TOPIC_PROPOSED -> setOf(CommissionStatus.REQUESTED)
                CommissionStatus.ACCEPTED -> setOf(CommissionStatus.TOPIC_PROPOSED)
                CommissionStatus.REJECTED -> setOf(CommissionStatus.REQUESTED)
                CommissionStatus.CANCELLED -> setOf(CommissionStatus.REQUESTED)
            }
        require(status in allowed) {
            "Cannot transition from $status to $target"
        }
        this.status = target
    }
}
