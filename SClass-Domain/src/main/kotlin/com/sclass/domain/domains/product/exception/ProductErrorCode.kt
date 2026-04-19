package com.sclass.domain.domains.product.exception

import com.sclass.common.exception.ErrorCode

enum class ProductErrorCode(
    override val code: String,
    override val message: String,
    override val httpStatus: Int,
) : ErrorCode {
    PRODUCT_NOT_FOUND("PRODUCT_001", "상품을 찾을 수 없습니다", 404),
    PRODUCT_TYPE_MISMATCH("PRODUCT_002", "상품 타입이 올바르지 않습니다", 400),
    PRODUCT_NOT_PURCHASABLE("PRODUCT_003", "현재 구매할 수 없는 상품입니다", 400),
    UNKNOWN_PRODUCT_TYPE("PRODUCT_004", "알 수 없는 상품 타입입니다", 500),
    INVALID_COHORT_PERIOD("PRODUCT_005", "기수 기간이 유효하지 않습니다 (startAt < endAt 필요)", 400),
    COHORT_SALE_ENDED("PRODUCT_006", "기수 판매가 종료되었습니다", 400),
}
