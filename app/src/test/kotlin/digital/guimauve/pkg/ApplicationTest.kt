package digital.guimauve.pkg

import digital.guimauve.pkg.domain.repositories.OrganizationsRepository
import digital.guimauve.pkg.domain.repositories.PackageVersionFilesRepository
import digital.guimauve.pkg.domain.repositories.PackageVersionsRepository
import digital.guimauve.pkg.domain.repositories.PackagesRepository
import digital.guimauve.pkg.domain.usecases.users.CreateUserUseCase
import digital.guimauve.pkg.models.organizations.CreateOrganizationPayload
import digital.guimauve.pkg.models.packages.CreatePackagePayload
import digital.guimauve.pkg.models.packages.Package
import digital.guimauve.pkg.models.packages.PackageFormat
import digital.guimauve.pkg.models.packages.versions.CreatePackageVersionPayload
import digital.guimauve.pkg.models.packages.versions.PackageVersion
import digital.guimauve.pkg.models.packages.versions.files.CreatePackageVersionFilePayload
import digital.guimauve.pkg.models.users.CreateUserPayload
import digital.guimauve.pkg.models.users.UserContext
import digital.guimauve.pkg.services.storage.FileContext
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import org.koin.ktor.ext.get
import java.io.ByteArrayInputStream
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class ApplicationTest {

    private fun withApplication(
        seed: suspend Application.() -> Unit = {},
        block: suspend ApplicationTestBuilder.() -> Unit,
    ) = testApplication {
        environment {
            config = ApplicationConfig("application.test.conf")
        }
        application {
            module()
            runBlocking { seed() }
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
     */
    @Test
    fun testDashboardRedirectsToLogin() = withApplication {
        val client = client.config { followRedirects = false }
        listOf("/packages", "/users").forEach { path ->
            val response = client.get(path)
            assertEquals(HttpStatusCode.Found, response.status, "unexpected status for $path")
            assertEquals("/auth/login?redirect=$path", response.headers[HttpHeaders.Location])
        }
    }

    @Test
    fun testRootRedirectsToPackages() = withApplication {
        val response = client.config { followRedirects = false }.get("/")
        assertEquals(HttpStatusCode.Found, response.status)
        assertEquals("/packages", response.headers[HttpHeaders.Location])
    }

    /**
     * The login page renders through the layout, so this covers the `t` directive too.
     */
    @Test
    fun testLoginPageIsServed() = withApplication {
        val response = client.get("/auth/login")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertContains(body, "<title>Sign in — PKG</title>")
        assertContains(body, """<label for="email" class="form-label">Email address</label>""")
    }

    /**
     * Renders every dashboard page of a signed in user, which is what catches a view field the
     * templates do not know about.
     */
    @Test
    fun testDashboardPagesRender() {
        val email = "dashboard@guimauve.digital"
        val password = "correct horse battery staple"
        lateinit var pkg: Package
        lateinit var version: PackageVersion
        withApplication(seed = {
            val organization = get<OrganizationsRepository>().create(CreateOrganizationPayload("Guimauve Digital"))!!
            val user = get<CreateUserUseCase>()(CreateUserPayload(email, password), organization.id)!!
            pkg = get<PackagesRepository>().create(
                CreatePackagePayload("com.example:library", PackageFormat.MAVEN, isPublic = true),
                organization.id
            )!!
            version = get<PackageVersionsRepository>().create(
                CreatePackageVersionPayload("1.0.0", metadata = null),
                pkg.id,
                UserContext(user.id)
            )!!
            get<PackageVersionFilesRepository>().create(
                CreatePackageVersionFilePayload("library-1.0.0.jar", pkg, version),
                version.id,
                FileContext(ByteArrayInputStream(ByteArray(0)), ContentType.Application.OctetStream, 2048)
            )!!
        }) {
            val client = client.config {
                install(HttpCookies)
                followRedirects = false
            }

            val login = client.submitForm(
                url = "/auth/login",
                formParameters = parameters {
                    append("email", email)
                    append("password", password)
                }
            )
            assertEquals(HttpStatusCode.Found, login.status, "sign in failed")

            val packages = client.get("/packages")
            assertEquals(HttpStatusCode.OK, packages.status)
            assertContains(packages.bodyAsText(), "com.example:library")

            val packageDetail = client.get("/packages/${pkg.id}")
            assertEquals(HttpStatusCode.OK, packageDetail.status)
            assertContains(packageDetail.bodyAsText(), "1.0.0")

            val versionDetail = client.get("/packages/${pkg.id}/versions/${version.id}")
            assertEquals(HttpStatusCode.OK, versionDetail.status)
            with(versionDetail.bodyAsText()) {
                assertContains(this, "library-1.0.0.jar")
                assertContains(this, "2.0 KB")
            }

            val users = client.get("/users")
            assertEquals(HttpStatusCode.OK, users.status)
            assertContains(users.bodyAsText(), email)
        }
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
