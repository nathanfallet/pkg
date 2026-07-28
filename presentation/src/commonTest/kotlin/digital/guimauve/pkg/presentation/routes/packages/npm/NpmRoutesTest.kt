package digital.guimauve.pkg.presentation.routes.packages.npm

import digital.guimauve.pkg.domain.models.packages.PackageFormat
import digital.guimauve.pkg.domain.models.packages.versions.PackageVersion
import digital.guimauve.pkg.domain.usecases.packages.GetPackageByNameUseCase
import digital.guimauve.pkg.domain.usecases.packages.versions.GetPackageVersionByNameUseCase
import digital.guimauve.pkg.domain.usecases.users.GetUserUseCase
import digital.guimauve.pkg.presentation.routes.RoutesTestHelper
import digital.guimauve.pkg.presentation.routes.RoutesTestHelper.configureTestApplication
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class NpmRoutesTest {

    private val version = PackageVersion(
        id = RoutesTestHelper.TEST_VERSION_ID,
        packageId = RoutesTestHelper.TEST_PACKAGE_ID,
        version = "1.0.0",
        publishedBy = RoutesTestHelper.TEST_USER_ID,
        publishedAt = Instant.parse("2026-01-02T03:04:05Z"),
        metadata = null,
        yanked = false,
    )

    private class Mocks {
        val getPackageByNameUseCase = mockk<GetPackageByNameUseCase>()
        val getPackageVersionByNameUseCase = mockk<GetPackageVersionByNameUseCase>()
        val getUserUseCase = mockk<GetUserUseCase>()
    }

    private fun ApplicationTestBuilder.configureApp(mocks: Mocks) {
        application {
            configureTestApplication(mocks.getUserUseCase)
            routing {
                authenticate("api-jwt", optional = true) {
                    npmRoutes(
                        NpmRoutesDependencies(
                            getPackageByNameUseCase = mocks.getPackageByNameUseCase,
                            getPackageVersionByNameUseCase = mocks.getPackageVersionByNameUseCase,
                            getUserUseCase = mocks.getUserUseCase,
                        )
                    )
                }
            }
        }
    }

    @Test
    fun testGetPackage() = testApplication {
        val mocks = Mocks()
        coEvery { mocks.getPackageByNameUseCase("library", PackageFormat.NPM, null) } returns
                RoutesTestHelper.testPackage
        configureApp(mocks)

        val response = client.get("/npm/library")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(
            """{"_id":"com.example:library","name":"com.example:library","versions":{}}""",
            response.bodyAsText()
        )
    }

    @Test
    fun testGetUnknownPackage() = testApplication {
        val mocks = Mocks()
        coEvery { mocks.getPackageByNameUseCase(any(), any(), any()) } returns null
        configureApp(mocks)

        val response = client.get("/npm/library")

        assertEquals(HttpStatusCode.NotFound, response.status)
        assertEquals("""{"error":"packages_not_found"}""", response.bodyAsText())
    }

    @Test
    fun testGetVersion() = testApplication {
        val mocks = Mocks()
        coEvery { mocks.getPackageByNameUseCase("library", PackageFormat.NPM, null) } returns
                RoutesTestHelper.testPackage
        coEvery { mocks.getPackageVersionByNameUseCase("1.0.0", RoutesTestHelper.TEST_PACKAGE_ID) } returns version
        configureApp(mocks)

        val response = client.get("/npm/library/1.0.0")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("""{"name":"com.example:library","version":"1.0.0"}""", response.bodyAsText())
    }

    @Test
    fun testGetUnknownVersion() = testApplication {
        val mocks = Mocks()
        coEvery { mocks.getPackageByNameUseCase(any(), any(), any()) } returns RoutesTestHelper.testPackage
        coEvery { mocks.getPackageVersionByNameUseCase(any(), any()) } returns null
        configureApp(mocks)

        val response = client.get("/npm/library/9.9.9")

        assertEquals(HttpStatusCode.NotFound, response.status)
        assertEquals("""{"error":"package_versions_not_found"}""", response.bodyAsText())
    }

    @Test
    fun testPublishAnonymously() = testApplication {
        val mocks = Mocks()
        configureApp(mocks)

        val response = client.put("/npm/library") {
            contentType(ContentType.Application.Json)
            setBody("""{"_id":"library","name":"library","versions":{}}""")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals("""{"error":"auth_invalid_credentials"}""", response.bodyAsText())
    }

}
