package com.sclass.backoffice.partnership.controller

import com.sclass.backoffice.partnership.dto.PartnershipLeadDetailResponse
import com.sclass.backoffice.partnership.dto.UpdatePartnershipLeadStatusRequest
import com.sclass.backoffice.partnership.usecase.GetPartnershipLeadsUseCase
import com.sclass.backoffice.partnership.usecase.UpdatePartnershipLeadStatusUseCase
import com.sclass.common.dto.ApiResponse
import com.sclass.domain.domains.partnership.domain.PartnershipLeadStatus
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/partnership-leads")
class PartnershipLeadAdminController(
    private val getPartnershipLeadsUseCase: GetPartnershipLeadsUseCase,
    private val updatePartnershipLeadStatusUseCase: UpdatePartnershipLeadStatusUseCase,
) {
    @GetMapping
    fun list(
        @RequestParam(required = false) status: PartnershipLeadStatus?,
        @PageableDefault(size = 20, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable,
    ): ApiResponse<Page<PartnershipLeadDetailResponse>> = ApiResponse.success(getPartnershipLeadsUseCase.execute(status, pageable))

    @PatchMapping("/{id}/status")
    fun updateStatus(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdatePartnershipLeadStatusRequest,
    ): ApiResponse<PartnershipLeadDetailResponse> = ApiResponse.success(updatePartnershipLeadStatusUseCase.execute(id, request))
}
