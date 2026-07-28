package digital.guimauve.pkg

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    private fun withApplication(block: suspend ApplicationTestBuilder.() -> Unit) = testApplication {
        environment {
            config = ApplicationConfig("application.test.conf")
        }
        application {
            module()
        }
        block()
    }

    @Test
    fun testStartup() = withApplication {
        val response = client.get("/api/v1/organizations")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    /**
     * Every nested resource must resolve to its route and answer 401 rather than 404, which would
     * mean the route is not registered at all.
     */
    @Test
    fun testNestedResourcesRequireAuthentication() = withApplication {
        val organizationId = "00000000-0000-4000-8000-000000000001"
        val userId = "00000000-0000-4000-8000-000000000002"
        val packageId = "00000000-0000-4000-8000-000000000003"
        val versionId = "00000000-0000-4000-8000-000000000004"
        val paths = listOf(
            "/api/v1/organizations/$organizationId",
            "/api/v1/organizations/$organizationId/users",
            "/api/v1/organizations/$organizationId/users/$userId",
            "/api/v1/organizations/$organizationId/packages",
            "/api/v1/organizations/$organizationId/packages/$packageId",
            "/api/v1/organizations/$organizationId/packages/$packageId/versions",
            "/api/v1/organizations/$organizationId/packages/$packageId/versions/$versionId",
            "/api/v1/organizations/$organizationId/packages/$packageId/versions/$versionId/files",
        )
        paths.forEach { path ->
            val response = client.get(path)
            assertEquals(HttpStatusCode.Unauthorized, response.status, "unexpected status for $path")
            assertEquals(
                """{"error":"auth_invalid_credentials"}""",
                response.bodyAsText(),
                "unexpected body for $path"
            )
        }
    }

    /**
     * The dashboard sends an anonymous visitor to the login page instead of answering 401.
     * `/packages` is first redirected to its localized path by the i18n plugin.
     */
    @Test
    fun testDashboardRedirectsToLogin() = withApplication {
        val client = client.config { followRedirects = false }

        val localized = client.get("/packages")
        assertEquals(HttpStatusCode.Found, localized.status)
        assertEquals("/en/packages", localized.headers[HttpHeaders.Location])

        val login = client.get("/en/packages")
        assertEquals(HttpStatusCode.Found, login.status)
        assertEquals("/auth/login?redirect=/en/packages", login.headers[HttpHeaders.Location])
    }

    @Test
    fun testLoginPageIsServed() = withApplication {
        val response = client.get("/auth/login")
        assertEquals(HttpStatusCode.OK, response.status)
    }

}
