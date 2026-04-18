package com.sclass.supporters.catalog.controller

import com.sclass.common.annotation.Public
import com.sclass.common.dto.ApiResponse
import com.sclass.supporters.catalog.dto.CatalogCourseDetailResponse
import com.sclass.supporters.catalog.dto.CatalogCoursePageResponse
import com.sclass.supporters.catalog.usecase.GetCatalogCourseDetailUseCase
import com.sclass.supporters.catalog.usecase.GetCatalogCourseListUseCase
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Public
@RestController
@RequestMapping("/api/v1/catalog")
class CatalogController(
    private val getCatalogCourseListUseCase: GetCatalogCourseListUseCase,
    private val getCatalogCourseDetailUseCase: GetCatalogCourseDetailUseCase,
) {
    @GetMapping("/courses")
    fun getCourseList(
        @PageableDefault(size = 20, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable,
    ): ApiResponse<CatalogCoursePageResponse> = ApiResponse.success(getCatalogCourseListUseCase.execute(pageable))

    @GetMapping("/courses/{courseId}")
    fun getCourseDetail(
        @PathVariable courseId: Long,
    ): ApiResponse<CatalogCourseDetailResponse> = ApiResponse.success(getCatalogCourseDetailUseCase.execute(courseId))
}
