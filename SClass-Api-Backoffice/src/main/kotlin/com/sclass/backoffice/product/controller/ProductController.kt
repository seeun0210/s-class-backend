package com.sclass.backoffice.product.controller

import com.sclass.backoffice.product.dto.ProductListResponse
import com.sclass.backoffice.product.usecase.DeactivateProductUseCase
import com.sclass.backoffice.product.usecase.GetAdminProductListUseCase
import com.sclass.common.dto.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/products")
class ProductController(
    private val deactivateProductUseCase: DeactivateProductUseCase,
    private val getProductListUseCase: GetAdminProductListUseCase,
) {
    @GetMapping
    fun getProducts(): ApiResponse<ProductListResponse> = ApiResponse.success(getProductListUseCase.execute())

    @PatchMapping("/{id}/deactivate")
    fun deactivateProduct(
        @PathVariable id: String,
    ): ApiResponse<Unit> {
        deactivateProductUseCase.execute(id)
        return ApiResponse.success(Unit)
    }
}
