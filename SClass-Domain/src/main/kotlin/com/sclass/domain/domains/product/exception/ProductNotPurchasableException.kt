package com.sclass.domain.domains.product.exception

import com.sclass.common.exception.BusinessException

class ProductNotPurchasableException : BusinessException(ProductErrorCode.PRODUCT_NOT_PURCHASABLE)
