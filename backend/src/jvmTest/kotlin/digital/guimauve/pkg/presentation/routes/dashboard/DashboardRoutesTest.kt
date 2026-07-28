package digital.guimauve.pkg.presentation.routes.dashboard

import digital.guimauve.pkg.domain.usecases.organizations.GetOrganizationUseCase
import digital.guimauve.pkg.domain.usecases.packages.GetPackageUseCase
import digital.guimauve.pkg.domain.usecases.packages.ListPackagesUseCase
import digital.guimauve.pkg.domain.usecases.packages.versions.GetPackageVersionUseCase
import digital.guimauve.pkg.domain.usecases.packages.versions.ListPackageVersionsUseCase
import digital.guimauve.pkg.domain.usecases.packages.versions.files.ListPackageVersionFilesUseCase
import digital.guimauve.pkg.domain.usecases.users.GetUserInOrganizationUseCase
import digital.guimauve.pkg.domain.usecases.users.GetUserUseCase
import digital.guimauve.pkg.domain.usecases.users.ListUsersUseCase
import digital.guimauve.pkg.models.packages.versions.PackageVersion
import digital.guimauve.pkg.models.packages.versions.files.PackageVersionFile
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
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

class DashboardRoutesTest {

    private val version = PackageVersion(
        id = RoutesTestHelper.TEST_VERSION_ID,
        packageId = RoutesTestHelper.TEST_PACKAGE_ID,
        version = "1.0.0",
        publishedBy = RoutesTestHelper.TEST_USER_ID,
        publishedAt = Instant.parse("2026-01-02T03:04:05Z"),
        metadata = null,
        yanked = false,
    )

    private val file = PackageVersionFile(
        id = Uuid.parse("00000000-0000-4000-8000-000000000005"),
        versionId = RoutesTestHelper.TEST_VERSION_ID,
        name = "library-1.0.0.jar",
        contentType = "application/java-archive",
        size = 2048,
        path = "some/path/library-1.0.0.jar",
    )

    private class Mocks {
        val listPackagesUseCase = mockk<ListPackagesUseCase>()
        val getPackageUseCase = mockk<GetPackageUseCase>()
        val listPackageVersionsUseCase = mockk<ListPackageVersionsUseCase>()
        val getPackageVersionUseCase = mockk<GetPackageVersionUseCase>()
        val listPackageVersionFilesUseCase = mockk<ListPackageVersionFilesUseCase>()
        val listUsersUseCase = mockk<ListUsersUseCase>()
        val getUserInOrganizationUseCase = mockk<GetUserInOrganizationUseCase>()
        val getOrganizationUseCase = mockk<GetOrganizationUseCase>()
        val getUserUseCase = mockk<GetUserUseCase>()
    }

    private fun ApplicationTestBuilder.configureApp(mocks: Mocks) {
        application {
            configureTestApplication(mocks.getUserUseCase)
            routing {
                authenticate("api-jwt", optional = true) {
                    dashboardRoutes(
                        DashboardRoutesDependencies(
                            listPackagesUseCase = mocks.listPackagesUseCase,
                            getPackageUseCase = mocks.getPackageUseCase,
                            listPackageVersionsUseCase = mocks.listPackageVersionsUseCase,
                            getPackageVersionUseCase = mocks.getPackageVersionUseCase,
                            listPackageVersionFilesUseCase = mocks.listPackageVersionFilesUseCase,
                            listUsersUseCase = mocks.listUsersUseCase,
                            getUserInOrganizationUseCase = mocks.getUserInOrganizationUseCase,
                            getOrganizationUseCase = mocks.getOrganizationUseCase,
                            getUserUseCase = mocks.getUserUseCase,
                        )
                    )
                }
            }
        }
    }

    /**
     * Signs every request in as [RoutesTestHelper.testUser].
     */
    private fun signedIn(mocks: Mocks): HttpRequestBuilder.() -> Unit {
        coEvery { mocks.getUserUseCase(RoutesTestHelper.TEST_USER_ID) } returns RoutesTestHelper.testUser
        coEvery { mocks.getOrganizationUseCase(RoutesTestHelper.TEST_ORGANIZATION_ID) } returns
                RoutesTestHelper.testOrganization
        return { bearerAuth(RoutesTestHelper.generateTestToken()) }
    }

    @Test
    fun testRootRedirectsToPackages() = testApplication {
        configureApp(Mocks())

        val response = client.config { followRedirects = false }.get("/")

        assertEquals(HttpStatusCode.Found, response.status)
        assertEquals("/packages", response.headers[HttpHeaders.Location])
    }

    @Test
    fun testAnonymousVisitorIsSentToLogin() = testApplication {
        configureApp(Mocks())
        val client = client.config { followRedirects = false }

        listOf("/packages", "/users").forEach { path ->
            val response = client.get(path)
            assertEquals(HttpStatusCode.Found, response.status, "unexpected status for $path")
            assertEquals("/auth/login?redirect=$path", response.headers[HttpHeaders.Location])
        }
    }

    @Test
    fun testPackagesPage() = testApplication {
        val mocks = Mocks()
        val auth = signedIn(mocks)
        coEvery { mocks.listPackagesUseCase(RoutesTestHelper.TEST_ORGANIZATION_ID) } returns
                listOf(RoutesTestHelper.testPackage)
        configureApp(mocks)

        val response = client.get("/packages", auth)

        assertEquals(HttpStatusCode.OK, response.status)
        with(response.bodyAsText()) {
            assertContains(this, "com.example:library")
            assertContains(this, "Public")
            assertContains(this, "2026-01-02 03:04 UTC")
        }
    }

    @Test
    fun testPackagePage() = testApplication {
        val mocks = Mocks()
        val auth = signedIn(mocks)
        coEvery {
            mocks.getPackageUseCase(RoutesTestHelper.TEST_PACKAGE_ID, RoutesTestHelper.TEST_ORGANIZATION_ID)
        } returns RoutesTestHelper.testPackage
        coEvery { mocks.listPackageVersionsUseCase(RoutesTestHelper.TEST_PACKAGE_ID) } returns listOf(version)
        configureApp(mocks)

        val response = client.get("/packages/${RoutesTestHelper.TEST_PACKAGE_ID}", auth)

        assertEquals(HttpStatusCode.OK, response.status)
        assertContains(response.bodyAsText(), "1.0.0")
    }

    @Test
    fun testUnknownPackagePage() = testApplication {
        val mocks = Mocks()
        val auth = signedIn(mocks)
        coEvery { mocks.getPackageUseCase(any(), any()) } returns null
        configureApp(mocks)

        val response = client.get("/packages/${Uuid.random()}", auth)

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun testPackageVersionPage() = testApplication {
        val mocks = Mocks()
        val auth = signedIn(mocks)
        coEvery {
            mocks.getPackageUseCase(RoutesTestHelper.TEST_PACKAGE_ID, RoutesTestHelper.TEST_ORGANIZATION_ID)
        } returns RoutesTestHelper.testPackage
        coEvery {
            mocks.getPackageVersionUseCase(RoutesTestHelper.TEST_VERSION_ID, RoutesTestHelper.TEST_PACKAGE_ID)
        } returns version
        coEvery { mocks.listPackageVersionFilesUseCase(RoutesTestHelper.TEST_VERSION_ID) } returns listOf(file)
        configureApp(mocks)

        val response = client.get(
            "/packages/${RoutesTestHelper.TEST_PACKAGE_ID}/versions/${RoutesTestHelper.TEST_VERSION_ID}",
            auth
        )

        assertEquals(HttpStatusCode.OK, response.status)
        with(response.bodyAsText()) {
            assertContains(this, "library-1.0.0.jar")
            assertContains(this, "2.0 KB")
        }
    }

    @Test
    fun testUsersPages() = testApplication {
        val mocks = Mocks()
        val auth = signedIn(mocks)
        coEvery { mocks.listUsersUseCase(RoutesTestHelper.TEST_ORGANIZATION_ID) } returns
                listOf(RoutesTestHelper.testUser)
        coEvery {
            mocks.getUserInOrganizationUseCase(RoutesTestHelper.TEST_USER_ID, RoutesTestHelper.TEST_ORGANIZATION_ID)
        } returns RoutesTestHelper.testUser
        configureApp(mocks)

        val list = client.get("/users", auth)
        assertEquals(HttpStatusCode.OK, list.status)
        assertContains(list.bodyAsText(), RoutesTestHelper.testUser.email)

        val detail = client.get("/users/${RoutesTestHelper.TEST_USER_ID}", auth)
        assertEquals(HttpStatusCode.OK, detail.status)
        assertContains(detail.bodyAsText(), RoutesTestHelper.testOrganization.name)
    }

}
