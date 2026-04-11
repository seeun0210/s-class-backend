package com.sclass.backoffice.product.controller

import com.sclass.backoffice.product.dto.CreateCoinProductRequest
import com.sclass.backoffice.product.dto.CreateCommissionProductRequest
import com.sclass.backoffice.product.dto.CreateCourseProductRequest
import com.sclass.backoffice.product.dto.ProductListResponse
import com.sclass.backoffice.product.dto.ProductResponse
import com.sclass.backoffice.product.usecase.CreateCoinProductUseCase
import com.sclass.backoffice.product.usecase.CreateCommissionProductUseCase
import com.sclass.backoffice.product.usecase.CreateCourseProductUseCase
import com.sclass.backoffice.product.usecase.DeactivateProductUseCase
import com.sclass.backoffice.product.usecase.GetAdminProductListUseCase
import com.sclass.common.dto.ApiResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/products")
class ProductController(
    private val createCoinProductUseCase: CreateCoinProductUseCase,
    private val createCourseProductUseCase: CreateCourseProductUseCase,
    private val createCommissionProductUseCase: CreateCommissionProductUseCase,
    private val deactivateProductUseCase: DeactivateProductUseCase,
    private val getProductListUseCase: GetAdminProductListUseCase,
) {
    @GetMapping
    fun getProducts(): ApiResponse<ProductListResponse> = ApiResponse.success(getProductListUseCase.execute())

    @PostMapping("/coin")
    fun createCoinProduct(
        @RequestBody @Valid request: CreateCoinProductRequest,
    ): ApiResponse<ProductResponse> = ApiResponse.success(createCoinProductUseCase.execute(request))

    @PostMapping("/commission")
    fun createCommissionProduct(
        @RequestBody @Valid request: CreateCommissionProductRequest,
    ): ApiResponse<ProductResponse> = ApiResponse.success(createCommissionProductUseCase.execute(request))

    @PostMapping("/course")
    fun createCourseProduct(
        @RequestBody @Valid request: CreateCourseProductRequest,
    ): ApiResponse<ProductResponse> = ApiResponse.success(createCourseProductUseCase.execute(request))

    @PatchMapping("/{id}/deactivate")
    fun deactivateProduct(
        @PathVariable id: String,
    ): ApiResponse<Unit> {
        deactivateProductUseCase.execute(id)
        return ApiResponse.success(Unit)
    }
}
