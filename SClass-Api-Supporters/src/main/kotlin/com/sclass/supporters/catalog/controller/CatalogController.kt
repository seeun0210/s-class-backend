package com.sclass.supporters.catalog.controller

import com.sclass.common.dto.ApiResponse
import com.sclass.supporters.catalog.dto.CatalogCourseResponse
import com.sclass.supporters.catalog.usecase.GetCatalogCourseListUseCase
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/catalog")
class CatalogController(
    private val getCatalogCourseListUseCase: GetCatalogCourseListUseCase,
) {
    @GetMapping("/courses")
    fun getCourseList(): ApiResponse<List<CatalogCourseResponse>> = ApiResponse.success(getCatalogCourseListUseCase.execute())
}
