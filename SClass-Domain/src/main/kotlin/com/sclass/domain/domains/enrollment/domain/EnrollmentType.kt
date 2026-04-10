package com.sclass.domain.domains.enrollment.domain

enum class EnrollmentType {
    PURCHASE, // 학생이 PG 결제로 등록
    ADMIN_GRANT, // 백오피스/LMS 관리자가 직접 부여
    COMPLIMENTARY, // 무상 제공 (이벤트, 프로모션)
    TRIAL, // 체험 수강
}
