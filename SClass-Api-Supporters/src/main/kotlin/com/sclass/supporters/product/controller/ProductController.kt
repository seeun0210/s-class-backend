package com.sclass.supporters.product.controller

import com.sclass.common.dto.ApiResponse
import com.sclass.supporters.product.dto.CommissionProductResponse
import com.sclass.supporters.product.dto.ProductListResponse
import com.sclass.supporters.product.usecase.GetCommissionProductUseCase
import com.sclass.supporters.product.usecase.GetProductListUseCase
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/products")
class ProductController(
    private val getProductListUseCase: GetProductListUseCase,
    private val getCommissionProductUseCase: GetCommissionProductUseCase,
) {
    @GetMapping
    fun getProducts(): ApiResponse<ProductListResponse> = ApiResponse.success(getProductListUseCase.execute())

    @GetMapping("/commission")
    fun getCommissionProduct(): ApiResponse<CommissionProductResponse> = ApiResponse.success(getCommissionProductUseCase.execute())
}
