package com.sclass.backoffice.product.controller

import com.sclass.backoffice.product.dto.ProductListResponse
import com.sclass.backoffice.product.dto.SetProductVisibilityRequest
import com.sclass.backoffice.product.usecase.GetAdminProductListUseCase
import com.sclass.backoffice.product.usecase.SetProductVisibilityUseCase
import com.sclass.common.dto.ApiResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/products")
class ProductController(
    private val setProductVisibilityUseCase: SetProductVisibilityUseCase,
    private val getProductListUseCase: GetAdminProductListUseCase,
) {
    @GetMapping
    fun getProducts(): ApiResponse<ProductListResponse> = ApiResponse.success(getProductListUseCase.execute())

    @PatchMapping("/{id}/visibility")
    fun setVisibility(
        @PathVariable id: String,
        @Valid @RequestBody request: SetProductVisibilityRequest,
    ): ApiResponse<Unit> {
        setProductVisibilityUseCase.execute(id, request.visible)
        return ApiResponse.success(Unit)
    }
}
