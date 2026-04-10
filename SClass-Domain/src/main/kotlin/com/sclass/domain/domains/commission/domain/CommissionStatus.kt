package com.sclass.domain.domains.commission.domain

enum class CommissionStatus {
    REQUESTED,
    TOPIC_PROPOSED,
    ACCEPTED, // 학생이 주제 선택 → Lesson 생성 완료 (terminal)
    REJECTED,
    CANCELLED,
}
