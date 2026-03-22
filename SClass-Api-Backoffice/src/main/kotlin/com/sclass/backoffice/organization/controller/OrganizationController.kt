package com.sclass.backoffice.organization.controller

import com.sclass.backoffice.organization.dto.CreateOrganizationRequest
import com.sclass.backoffice.organization.dto.OrganizationPageResponse
import com.sclass.backoffice.organization.dto.OrganizationResponse
import com.sclass.backoffice.organization.usecase.CreateOrganizationUseCase
import com.sclass.backoffice.organization.usecase.GetOrganizationsUseCase
import com.sclass.common.dto.ApiResponse
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/organizations")
class OrganizationController(
    private val getOrganizationsUseCase: GetOrganizationsUseCase,
    private val createOrganizationUseCase: CreateOrganizationUseCase,
) {
    @GetMapping
    fun getOrganizations(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ApiResponse<OrganizationPageResponse> =
        ApiResponse.success(
            getOrganizationsUseCase.execute(
                PageRequest.of(page, size, Sort.by("id").descending()),
            ),
        )

    @PostMapping
    fun createOrganization(
        @Valid @RequestBody request: CreateOrganizationRequest,
    ): ApiResponse<OrganizationResponse> = ApiResponse.success(createOrganizationUseCase.execute(request))
}
