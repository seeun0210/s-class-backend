package com.sclass.backoffice.product.course.controller

import com.sclass.backoffice.product.course.dto.CourseProductPageResponse
import com.sclass.backoffice.product.course.dto.CourseProductResponse
import com.sclass.backoffice.product.course.dto.CreateCourseProductRequest
import com.sclass.backoffice.product.course.dto.UpdateCourseProductRequest
import com.sclass.backoffice.product.course.usecase.CreateCourseProductUseCase
import com.sclass.backoffice.product.course.usecase.GetCourseProductDetailUseCase
import com.sclass.backoffice.product.course.usecase.GetCourseProductListUseCase
import com.sclass.backoffice.product.course.usecase.UpdateCourseProductUseCase
import com.sclass.common.dto.ApiResponse
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/course-products")
class CourseProductController(
    private val createCourseProductUseCase: CreateCourseProductUseCase,
    private val updateCourseProductUseCase: UpdateCourseProductUseCase,
    private val getCourseProductListUseCase: GetCourseProductListUseCase,
    private val getCourseProductDetailUseCase: GetCourseProductDetailUseCase,
) {
    @PostMapping
    fun create(
        @Valid @RequestBody request: CreateCourseProductRequest,
    ): ApiResponse<CourseProductResponse> = ApiResponse.success(createCourseProductUseCase.execute(request))

    @GetMapping
    fun getList(
        @PageableDefault(size = 20, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable,
    ): ApiResponse<CourseProductPageResponse> = ApiResponse.success(getCourseProductListUseCase.execute(pageable))

    @GetMapping("/{productId}")
    fun getDetail(
        @PathVariable productId: String,
    ): ApiResponse<CourseProductResponse> = ApiResponse.success(getCourseProductDetailUseCase.execute(productId))

    @PutMapping("/{productId}")
    fun update(
        @PathVariable productId: String,
        @Valid @RequestBody request: UpdateCourseProductRequest,
    ): ApiResponse<CourseProductResponse> = ApiResponse.success(updateCourseProductUseCase.execute(productId, request))
}
