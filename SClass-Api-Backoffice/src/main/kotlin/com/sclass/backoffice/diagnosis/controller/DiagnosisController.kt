package com.sclass.backoffice.diagnosis.controller

import com.sclass.backoffice.diagnosis.dto.DiagnosisDetailResponse
import com.sclass.backoffice.diagnosis.dto.DiagnosisListItemResponse
import com.sclass.backoffice.diagnosis.usecase.GetDiagnosisDetailUseCase
import com.sclass.backoffice.diagnosis.usecase.GetDiagnosisListUseCase
import com.sclass.common.dto.ApiResponse
import com.sclass.domain.domains.diagnosis.domain.DiagnosisStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/diagnoses")
class DiagnosisController(
    private val getDiagnosisListUseCase: GetDiagnosisListUseCase,
    private val getDiagnosisDetailUseCase: GetDiagnosisDetailUseCase,
) {
    @GetMapping fun getList(
        @RequestParam(required = false) status: DiagnosisStatus?,
        @PageableDefault(size = 20, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable,
    ): ApiResponse<Page<DiagnosisListItemResponse>> = ApiResponse.success(getDiagnosisListUseCase.execute(status, pageable))

    @GetMapping("/{id}")
    fun getDetail(
        @PathVariable id: String,
    ): ApiResponse<DiagnosisDetailResponse> = ApiResponse.success(getDiagnosisDetailUseCase.execute(id))
}
