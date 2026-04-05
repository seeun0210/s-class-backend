package com.sclass.domain.domains.product.exception

import com.sclass.common.exception.BusinessException

class ProductNotFoundException : BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND)
