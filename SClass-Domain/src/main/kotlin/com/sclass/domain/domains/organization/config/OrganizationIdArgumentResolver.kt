package com.sclass.domain.domains.organization.config

import com.sclass.common.annotation.OrganizationId
import com.sclass.domain.domains.organization.adaptor.OrganizationAdaptor
import com.sclass.domain.domains.organization.exception.OrganizationNotFoundException
import com.sclass.domain.domains.organization.exception.OrganizationSubdomainNotResolvedException
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import java.net.URI

@Component
class OrganizationIdArgumentResolver(
    private val organizationAdaptor: OrganizationAdaptor,
) : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean = parameter.hasParameterAnnotation(OrganizationId::class.java)

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): Long? {
        val annotation = parameter.getParameterAnnotation(OrganizationId::class.java)!!
        val required = annotation.required

        val domain =
            extractDomain(webRequest)
                ?: if (required) throw OrganizationSubdomainNotResolvedException() else return null

        if (isLocalhost(domain)) {
            if (required) throw OrganizationSubdomainNotResolvedException() else return null
        }

        val organization =
            organizationAdaptor.findByDomainOrNull(domain)
                ?: if (required) throw OrganizationNotFoundException() else return null

        return organization.id
    }

    private fun extractDomain(webRequest: NativeWebRequest): String? {
        val origin = webRequest.getHeader("Origin")
        if (!origin.isNullOrBlank()) {
            return extractHostFromUrl(origin)
        }

        val referer = webRequest.getHeader("Referer")
        if (!referer.isNullOrBlank()) {
            return extractHostFromUrl(referer)
        }

        val host = webRequest.getHeader("Host")
        if (!host.isNullOrBlank()) {
            return removePort(host)
        }

        return null
    }

    private fun extractHostFromUrl(url: String): String? {
        return try {
            val host = URI(url).host ?: return null
            removePort(host)
        } catch (_: Exception) {
            null
        }
    }

    private fun removePort(host: String): String = host.split(":").first()

    private fun isLocalhost(domain: String): Boolean = domain == "localhost" || domain == "127.0.0.1"
}
