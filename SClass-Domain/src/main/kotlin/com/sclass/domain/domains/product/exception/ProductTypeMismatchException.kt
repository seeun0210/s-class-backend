package com.sclass.domain.domains.product.exception

import com.sclass.common.exception.BusinessException

class ProductTypeMismatchException : BusinessException(ProductErrorCode.PRODUCT_TYPE_MISMATCH)
