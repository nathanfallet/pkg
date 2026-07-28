package digital.guimauve.pkg

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

/**
 * Boots the real application, so these cover the wiring the route tests mock away: the Koin graph,
 * the database, and the shape of the routing tree. What each route answers is covered by the route
 * tests of the backend module.
 */
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
     * Renders a page of the dashboard, which is what proves the message bundle and the freemarker
     * directive are resolved through the real Koin graph.
     */
    @Test
    fun testLoginPageIsServed() = withApplication {
        val response = client.get("/auth/login")
        assertEquals(HttpStatusCode.OK, response.status)
        assertContains(response.bodyAsText(), "<title>Sign in — PKG</title>")
    }

    /**
     * A browser gets the error page, anything else gets the error key as JSON.
     */
    @Test
    fun testErrorsAreNegotiated() = withApplication {
        val json = client.get("/does-not-exist")
        assertEquals(HttpStatusCode.NotFound, json.status)
        assertEquals("""{"error":"error_not_found"}""", json.bodyAsText())

        val html = client.get("/does-not-exist") { header(HttpHeaders.Accept, ContentType.Text.Html.toString()) }
        assertEquals(HttpStatusCode.NotFound, html.status)
        assertContains(html.bodyAsText(), "This page does not exist")
    }

}
