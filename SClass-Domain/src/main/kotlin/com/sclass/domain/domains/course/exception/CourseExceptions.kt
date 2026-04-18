package com.sclass.domain.domains.course.exception

import com.sclass.common.exception.BusinessException

class CourseNotFoundException : BusinessException(CourseErrorCode.COURSE_NOT_FOUND)

class CourseNotListedException : BusinessException(CourseErrorCode.COURSE_NOT_LISTED)

class CourseInvalidStatusTransitionException : BusinessException(CourseErrorCode.COURSE_INVALID_STATUS_TRANSITION)

class CourseProductNotFoundException : BusinessException(CourseErrorCode.COURSE_PRODUCT_NOT_FOUND)

class CourseNotEnrollableException : BusinessException(CourseErrorCode.COURSE_NOT_ENROLLABLE)
