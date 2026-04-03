package com.sclass.backoffice.webhook.controller

import com.sclass.backoffice.webhook.dto.CreateWebhookRequest
import com.sclass.backoffice.webhook.dto.CreateWebhookResponse
import com.sclass.backoffice.webhook.dto.UpdateWebhookStatusRequest
import com.sclass.backoffice.webhook.dto.WebhookLogResponse
import com.sclass.backoffice.webhook.dto.WebhookResponse
import com.sclass.backoffice.webhook.usecase.CreateWebhookUseCase
import com.sclass.backoffice.webhook.usecase.DeleteWebhookUseCase
import com.sclass.backoffice.webhook.usecase.GetWebhookListUseCase
import com.sclass.backoffice.webhook.usecase.GetWebhookLogListUseCase
import com.sclass.backoffice.webhook.usecase.UpdateWebhookStatusUseCase
import com.sclass.common.dto.ApiResponse
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/webhooks")
class WebhookController(
    private val createWebhookUseCase: CreateWebhookUseCase,
    private val getWebhookListUseCase: GetWebhookListUseCase,
    private val updateWebhookStatusUseCase: UpdateWebhookStatusUseCase,
    private val deleteWebhookUseCase: DeleteWebhookUseCase,
    private val getWebhookLogListUseCase: GetWebhookLogListUseCase,
) {
    @PostMapping
    fun createWebhook(
        @Valid @RequestBody request: CreateWebhookRequest,
    ): ApiResponse<CreateWebhookResponse> = ApiResponse.success(createWebhookUseCase.execute(request))

    @GetMapping
    fun getWebhookList(): ApiResponse<List<WebhookResponse>> = ApiResponse.success(getWebhookListUseCase.execute())

    @PatchMapping("/{id}/status")
    fun updateWebhookStatus(
        @PathVariable id: String,
        @Valid @RequestBody request: UpdateWebhookStatusRequest,
    ): ApiResponse<Unit> {
        updateWebhookStatusUseCase.execute(id, request)
        return ApiResponse.success(Unit)
    }

    @DeleteMapping("/{id}")
    fun deleteWebhook(
        @PathVariable id: String,
    ): ApiResponse<Unit> {
        deleteWebhookUseCase.execute(id)
        return ApiResponse.success(Unit)
    }

    @GetMapping("/{id}/logs")
    fun getWebhookLogList(
        @PathVariable id: String,
        @PageableDefault(size = 20) pageable: Pageable,
    ): ApiResponse<Page<WebhookLogResponse>> = ApiResponse.success(getWebhookLogListUseCase.execute(id, pageable))
}
