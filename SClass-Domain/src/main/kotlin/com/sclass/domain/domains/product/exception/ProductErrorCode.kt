package com.sclass.domain.domains.product.exception

import com.sclass.common.exception.ErrorCode

enum class ProductErrorCode(
    override val code: String,
    override val message: String,
    override val httpStatus: Int,
) : ErrorCode {
    PRODUCT_NOT_FOUND("PRODUCT_001", "상품을 찾을 수 없습니다", 404),
}
