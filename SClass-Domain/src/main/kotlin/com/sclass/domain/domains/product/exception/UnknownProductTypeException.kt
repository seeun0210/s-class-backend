package com.sclass.domain.domains.product.exception

import com.sclass.common.exception.BusinessException

class UnknownProductTypeException : BusinessException(ProductErrorCode.UNKNOWN_PRODUCT_TYPE)
