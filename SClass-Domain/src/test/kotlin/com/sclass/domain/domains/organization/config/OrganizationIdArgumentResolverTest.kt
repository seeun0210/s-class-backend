package com.sclass.domain.domains.organization.config

import com.sclass.common.annotation.OrganizationId
import com.sclass.domain.domains.organization.adaptor.OrganizationAdaptor
import com.sclass.domain.domains.organization.domain.Organization
import com.sclass.domain.domains.organization.exception.OrganizationNotFoundException
import com.sclass.domain.domains.organization.exception.OrganizationSubdomainNotResolvedException
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.core.MethodParameter
import org.springframework.web.context.request.NativeWebRequest

class OrganizationIdArgumentResolverTest {
    private lateinit var organizationAdaptor: OrganizationAdaptor
    private lateinit var resolver: OrganizationIdArgumentResolver

    @BeforeEach
    fun setUp() {
        organizationAdaptor = mockk()
        resolver = OrganizationIdArgumentResolver(organizationAdaptor)
    }

    private fun mockWebRequest(
        origin: String? = null,
        referer: String? = null,
        host: String? = null,
    ): NativeWebRequest =
        mockk {
            every { getHeader("Origin") } returns origin
            every { getHeader("Referer") } returns referer
            every { getHeader("Host") } returns host
        }

    private fun mockParameter(required: Boolean = true): MethodParameter =
        mockk {
            every { hasParameterAnnotation(OrganizationId::class.java) } returns true
            every { getParameterAnnotation(OrganizationId::class.java) } returns
                mockk {
                    every { this@mockk.required } returns required
                }
        }

    @Nested
    inner class OriginHeader {
        @Test
        fun `Origin 헤더에서 도메인을 추출하여 Organization ID를 반환한다`() {
            val organization = mockk<Organization> { every { id } returns 1L }
            every { organizationAdaptor.findByDomainOrNull("academy.sclass.com") } returns organization
            val webRequest = mockWebRequest(origin = "https://academy.sclass.com")

            val result = resolver.resolveArgument(mockParameter(), null, webRequest, null)

            assertEquals(1L, result)
        }

        @Test
        fun `Origin이 Referer와 Host보다 우선한다`() {
            val organization = mockk<Organization> { every { id } returns 1L }
            every { organizationAdaptor.findByDomainOrNull("origin.sclass.com") } returns organization
            val webRequest =
                mockWebRequest(
                    origin = "https://origin.sclass.com",
                    referer = "https://referer.sclass.com/page",
                    host = "host.sclass.com",
                )

            val result = resolver.resolveArgument(mockParameter(), null, webRequest, null)

            assertEquals(1L, result)
        }
    }

    @Nested
    inner class RefererFallback {
        @Test
        fun `Origin이 없으면 Referer에서 도메인을 추출한다`() {
            val organization = mockk<Organization> { every { id } returns 2L }
            every { organizationAdaptor.findByDomainOrNull("academy.sclass.com") } returns organization
            val webRequest = mockWebRequest(referer = "https://academy.sclass.com/some/page")

            val result = resolver.resolveArgument(mockParameter(), null, webRequest, null)

            assertEquals(2L, result)
        }
    }

    @Nested
    inner class HostFallback {
        @Test
        fun `Origin과 Referer가 없으면 Host 헤더를 사용한다`() {
            val organization = mockk<Organization> { every { id } returns 3L }
            every { organizationAdaptor.findByDomainOrNull("academy.sclass.com") } returns organization
            val webRequest = mockWebRequest(host = "academy.sclass.com")

            val result = resolver.resolveArgument(mockParameter(), null, webRequest, null)

            assertEquals(3L, result)
        }

        @Test
        fun `Host 헤더에 포트가 포함되면 포트를 제거한다`() {
            val organization = mockk<Organization> { every { id } returns 3L }
            every { organizationAdaptor.findByDomainOrNull("academy.sclass.com") } returns organization
            val webRequest = mockWebRequest(host = "academy.sclass.com:8080")

            val result = resolver.resolveArgument(mockParameter(), null, webRequest, null)

            assertEquals(3L, result)
        }
    }

    @Nested
    inner class Localhost {
        @Test
        fun `localhost이고 required가 false이면 null을 반환한다`() {
            val webRequest = mockWebRequest(origin = "http://localhost:3000")

            val result = resolver.resolveArgument(mockParameter(required = false), null, webRequest, null)

            assertNull(result)
        }

        @Test
        fun `localhost이고 required가 true이면 OrganizationSubdomainNotResolvedException이 발생한다`() {
            val webRequest = mockWebRequest(origin = "http://localhost:3000")

            assertThrows<OrganizationSubdomainNotResolvedException> {
                resolver.resolveArgument(mockParameter(required = true), null, webRequest, null)
            }
        }

        @Test
        fun `127_0_0_1이고 required가 true이면 OrganizationSubdomainNotResolvedException이 발생한다`() {
            val webRequest = mockWebRequest(origin = "http://127.0.0.1:8080")

            assertThrows<OrganizationSubdomainNotResolvedException> {
                resolver.resolveArgument(mockParameter(required = true), null, webRequest, null)
            }
        }
    }

    @Nested
    inner class DomainNotFound {
        @Test
        fun `존재하지 않는 도메인이고 required가 true이면 OrganizationNotFoundException이 발생한다`() {
            every { organizationAdaptor.findByDomainOrNull("unknown.sclass.com") } returns null
            val webRequest = mockWebRequest(origin = "https://unknown.sclass.com")

            assertThrows<OrganizationNotFoundException> {
                resolver.resolveArgument(mockParameter(required = true), null, webRequest, null)
            }
        }

        @Test
        fun `존재하지 않는 도메인이고 required가 false이면 null을 반환한다`() {
            every { organizationAdaptor.findByDomainOrNull("unknown.sclass.com") } returns null
            val webRequest = mockWebRequest(origin = "https://unknown.sclass.com")

            val result = resolver.resolveArgument(mockParameter(required = false), null, webRequest, null)

            assertNull(result)
        }
    }

    @Nested
    inner class NoHeaders {
        @Test
        fun `헤더가 모두 없고 required가 true이면 OrganizationSubdomainNotResolvedException이 발생한다`() {
            val webRequest = mockWebRequest()

            assertThrows<OrganizationSubdomainNotResolvedException> {
                resolver.resolveArgument(mockParameter(required = true), null, webRequest, null)
            }
        }

        @Test
        fun `헤더가 모두 없고 required가 false이면 null을 반환한다`() {
            val webRequest = mockWebRequest()

            val result = resolver.resolveArgument(mockParameter(required = false), null, webRequest, null)

            assertNull(result)
        }
    }
}
