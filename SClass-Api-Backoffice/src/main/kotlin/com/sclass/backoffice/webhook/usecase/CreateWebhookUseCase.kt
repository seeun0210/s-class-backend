package com.sclass.backoffice.webhook.usecase

import com.sclass.backoffice.webhook.dto.CreateWebhookRequest
import com.sclass.backoffice.webhook.dto.CreateWebhookResponse
import com.sclass.backoffice.webhook.dto.WebhookFieldMappingResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.webhook.adaptor.WebhookAdaptor
import com.sclass.domain.domains.webhook.domain.Webhook
import com.sclass.domain.domains.webhook.domain.WebhookFieldMapping
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@UseCase
class CreateWebhookUseCase(
    private val webhookAdaptor: WebhookAdaptor,
) {
    @Transactional
    fun execute(request: CreateWebhookRequest): CreateWebhookResponse {
        val webhook =
            Webhook.create(
                name = request.name,
                type = request.type,
                fieldMapping =
                    WebhookFieldMapping(
                        studentNameQuestion = request.fieldMapping.studentNameQuestion,
                        studentPhoneQuestion = request.fieldMapping.studentPhoneQuestion,
                        parentPhoneQuestion = request.fieldMapping.parentPhoneQuestion,
                    ),
            )
        val saved = webhookAdaptor.save(webhook)
        val scriptCode = generateScriptCode(saved.id, saved.secret)

        return CreateWebhookResponse(
            id = saved.id,
            name = saved.name,
            type = saved.type,
            secret = saved.secret,
            scriptCode = scriptCode,
            fieldMapping = WebhookFieldMappingResponse.from(saved.fieldMapping),
        )
    }

    private fun generateScriptCode(
        webhookId: String,
        secret: String,
    ): String {
        val baseUrl =
            ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .build()
                .toUriString()
        return """
            function onFormSubmit(e) {
              try {
                if (!e || !e.response) {
                  Logger.log('[webhook] no event response, skip');
                  return;
                }

                var responses = e.response.getItemResponses();
                var answers = {};
                responses.forEach(function (r) {
                  answers[r.getItem().getTitle()] = r.getResponse();
                });

                var payload = {
                  formId: FormApp.getActiveForm().getId(),
                  formTitle: FormApp.getActiveForm().getTitle(),
                  formResponseId: e.response.getId(),
                  submittedAt: e.response.getTimestamp().toISOString(),
                  answers: answers
                };

                var url = '$baseUrl/webhook/$webhookId';
                var secret = '$secret';

                var res = UrlFetchApp.fetch(url, {
                  method: 'post',
                  contentType: 'application/json',
                  headers: { 'X-Webhook-Secret': secret },
                  payload: JSON.stringify(payload),
                  muteHttpExceptions: true
                });

                var code = res.getResponseCode();
                var body = res.getContentText();
                Logger.log('[webhook] status=' + code);
                Logger.log(body);

                // 실패 시 1회 재시도
                if (code < 200 || code >= 300) {
                  Utilities.sleep(1200);
                  var retry = UrlFetchApp.fetch(url, {
                    method: 'post',
                    contentType: 'application/json',
                    headers: { 'X-Webhook-Secret': secret },
                    payload: JSON.stringify(payload),
                    muteHttpExceptions: true
                  });
                  Logger.log('[webhook][retry] status=' + retry.getResponseCode());
                  Logger.log(retry.getContentText());
                }
              } catch (err) {
                Logger.log('[webhook][error] ' + (err && err.message ? err.message : err));
                throw err;
              }
            }
            """.trimIndent()
    }
}
