package com.sclass.supporters.catalog.controller

import com.sclass.common.annotation.Public
import com.sclass.common.dto.ApiResponse
import com.sclass.domain.domains.product.domain.ProductCatalogSort
import com.sclass.domain.domains.product.domain.ProductType
import com.sclass.supporters.catalog.dto.CatalogProductPageResponse
import com.sclass.supporters.catalog.dto.CatalogProductResponse
import com.sclass.supporters.catalog.usecase.GetCatalogProductDetailUseCase
import com.sclass.supporters.catalog.usecase.GetCatalogProductListUseCase
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Public
@RestController
@RequestMapping("/api/v1/catalog")
class CatalogController(
    private val getCatalogProductListUseCase: GetCatalogProductListUseCase,
    private val getCatalogProductDetailUseCase: GetCatalogProductDetailUseCase,
) {
    @GetMapping("/products")
    fun getProductList(
        @RequestParam(required = false, name = "type") types: List<ProductType>?,
        @RequestParam(defaultValue = "LATEST") sort: ProductCatalogSort,
        @PageableDefault(size = 20) pageable: Pageable,
    ): ApiResponse<CatalogProductPageResponse> = ApiResponse.success(getCatalogProductListUseCase.execute(types, sort, pageable))

    @GetMapping("/products/{productId}")
    fun getProductDetail(
        @PathVariable productId: String,
    ): ApiResponse<CatalogProductResponse> = ApiResponse.success(getCatalogProductDetailUseCase.execute(productId))
}
