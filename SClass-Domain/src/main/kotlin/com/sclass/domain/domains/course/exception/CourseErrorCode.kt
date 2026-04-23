package com.sclass.domain.domains.course.exception

import com.sclass.common.exception.ErrorCode

enum class CourseErrorCode(
    override val code: String,
    override val message: String,
    override val httpStatus: Int,
) : ErrorCode {
    COURSE_NOT_FOUND("COURSE_001", "코스를 찾을 수 없습니다", 404),
    COURSE_NOT_LISTED("COURSE_002", "판매 중인 코스가 아닙니다", 400),
    COURSE_INVALID_STATUS_TRANSITION("COURSE_003", "잘못된 코스 상태 전이입니다", 400),
    COURSE_PRODUCT_NOT_FOUND("COURSE_004", "코스에 연결된 상품을 찾을 수 없습니다", 404),
    COURSE_NOT_ENROLLABLE("COURSE_005", "등록할 수 없는 코스입니다", 400),
    COURSE_ALREADY_STARTED("COURSE_006", "이미 시작된 코스는 일정/모집 조건을 변경할 수 없습니다", 400),
    COURSE_MAX_ENROLLMENTS_TOO_LOW("COURSE_007", "최대 정원은 현재 등록자 수보다 작을 수 없습니다", 400),
    COURSE_INVALID_SCHEDULE("COURSE_008", "모집 시작 < 모집 마감 ≤ 개강 < 종강 순이어야 합니다", 400),
    COURSE_MATCHING_PRODUCT_NOT_CREATABLE("COURSE_009", "매칭형 코스 상품으로는 일반 코스를 생성할 수 없습니다", 400),
    COURSE_PRODUCT_ALREADY_IN_USE("COURSE_010", "이미 운영 코스에 연결된 일반 코스 상품입니다", 400),
    COURSE_MATCHING_PRODUCT_NOT_CONVERTIBLE("COURSE_011", "여러 코스에 연결된 매칭형 코스 상품은 일반 코스 상품으로 전환할 수 없습니다", 400),
    COURSE_MATCHING_PRODUCT_HAS_PENDING_MATCH_ENROLLMENT("COURSE_012", "매칭 대기 중인 수강 등록이 있는 매칭형 코스 상품은 일반 코스 상품으로 전환할 수 없습니다", 400),
}
