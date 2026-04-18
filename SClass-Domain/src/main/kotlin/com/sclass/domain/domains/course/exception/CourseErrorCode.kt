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
}
