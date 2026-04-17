package com.sclass.domain.domains.product.exception

import com.sclass.common.exception.ErrorCode

enum class ProductErrorCode(
    override val code: String,
    override val message: String,
    override val httpStatus: Int,
) : ErrorCode {
    PRODUCT_NOT_FOUND("PRODUCT_001", "상품을 찾을 수 없습니다", 404),
    PRODUCT_TYPE_MISMATCH("PRODUCT_002", "상품 타입이 올바르지 않습니다", 400),
    UNKNOWN_PRODUCT_TYPE("PRODUCT_004", "알 수 없는 상품 타입입니다", 500),
}
