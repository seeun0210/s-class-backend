package com.sclass.domain.domains.enrollment.domain

enum class EnrollmentStatus {
    PENDING_PAYMENT, // 결제 대기 (PURCHASE만 사용)
    ACTIVE, // 수강 중
    COMPLETED, // 전 차시 완료
    CANCELLED, // 취소
    REFUNDED, // 환불 완료 (PURCHASE만)
}
