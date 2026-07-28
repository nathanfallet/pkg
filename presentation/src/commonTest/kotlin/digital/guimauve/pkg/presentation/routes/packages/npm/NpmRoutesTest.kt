package digital.guimauve.pkg.presentation.routes.packages.npm

import digital.guimauve.pkg.domain.models.auth.LoginPayload
import digital.guimauve.pkg.domain.models.auth.TokenType
import digital.guimauve.pkg.domain.models.packages.PackageFormat
import digital.guimauve.pkg.domain.models.packages.versions.PackageVersion
import digital.guimauve.pkg.domain.models.packages.versions.files.PackageVersionFile
import digital.guimauve.pkg.domain.services.TokenService
import digital.guimauve.pkg.domain.usecases.auth.LoginUseCase
import digital.guimauve.pkg.domain.usecases.packages.GetOrCreatePackageUseCase
import digital.guimauve.pkg.domain.usecases.packages.GetPackageByNameUseCase
import digital.guimauve.pkg.domain.usecases.packages.versions.GetOrCreatePackageVersionUseCase
import digital.guimauve.pkg.domain.usecases.packages.versions.GetPackageVersionByNameUseCase
import digital.guimauve.pkg.domain.usecases.packages.versions.ListPackageVersionsUseCase
import digital.guimauve.pkg.domain.usecases.packages.versions.files.CreatePackageVersionFileUseCase
import digital.guimauve.pkg.domain.usecases.packages.versions.files.DownloadFileUseCase
import digital.guimauve.pkg.domain.usecases.packages.versions.files.GetLatestPackageVersionFileUseCase
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
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.time.Instant
import kotlin.uuid.Uuid

class NpmRoutesTest {

    private val manifest = """
        {"name":"@guimauve/fake-pkg","version":"1.0.0","main":"src/index.js",
         "dist":{"shasum":"abc","tarball":"http://elsewhere.example.com/whatever.tgz"}}
    """.trimIndent()

    private val version = PackageVersion(
        id = RoutesTestHelper.TEST_VERSION_ID,
        packageId = RoutesTestHelper.TEST_PACKAGE_ID,
        version = "1.0.0",
        publishedBy = RoutesTestHelper.TEST_USER_ID,
        publishedAt = Instant.parse("2026-01-02T03:04:05Z"),
        metadata = manifest,
        yanked = false,
    )

    private val tarball = PackageVersionFile(
        id = Uuid.parse("00000000-0000-4000-8000-000000000005"),
        versionId = RoutesTestHelper.TEST_VERSION_ID,
        name = "guimauve-fake-pkg-1.0.0.tgz",
        contentType = "application/octet-stream",
        size = 4,
        path = "some/path/guimauve-fake-pkg-1.0.0.tgz",
    )

    private val scopedPackage = RoutesTestHelper.testPackage.copy(
        name = "@guimauve/fake-pkg",
        format = PackageFormat.NPM,
    )

    private class Mocks {
        val getPackageByNameUseCase = mockk<GetPackageByNameUseCase>()
        val getOrCreatePackageUseCase = mockk<GetOrCreatePackageUseCase>()
        val getPackageVersionByNameUseCase = mockk<GetPackageVersionByNameUseCase>()
        val getOrCreatePackageVersionUseCase = mockk<GetOrCreatePackageVersionUseCase>()
        val listPackageVersionsUseCase = mockk<ListPackageVersionsUseCase>()
        val createPackageVersionFileUseCase = mockk<CreatePackageVersionFileUseCase>()
        val getLatestPackageVersionFileUseCase = mockk<GetLatestPackageVersionFileUseCase>()
        val downloadFileUseCase = mockk<DownloadFileUseCase>()
        val getUserUseCase = mockk<GetUserUseCase>()
        val loginUseCase = mockk<LoginUseCase>()
        val tokenService = mockk<TokenService>()
    }

    private fun ApplicationTestBuilder.configureApp(mocks: Mocks) {
        application {
            configureTestApplication(mocks.getUserUseCase)
            routing {
                authenticate("api-jwt", optional = true) {
                    npmRoutes(
                        NpmRoutesDependencies(
                            getPackageByNameUseCase = mocks.getPackageByNameUseCase,
                            getOrCreatePackageUseCase = mocks.getOrCreatePackageUseCase,
                            getPackageVersionByNameUseCase = mocks.getPackageVersionByNameUseCase,
                            getOrCreatePackageVersionUseCase = mocks.getOrCreatePackageVersionUseCase,
                            listPackageVersionsUseCase = mocks.listPackageVersionsUseCase,
                            createPackageVersionFileUseCase = mocks.createPackageVersionFileUseCase,
                            getLatestPackageVersionFileUseCase = mocks.getLatestPackageVersionFileUseCase,
                            downloadFileUseCase = mocks.downloadFileUseCase,
                            getUserUseCase = mocks.getUserUseCase,
                            loginUseCase = mocks.loginUseCase,
                            tokenService = mocks.tokenService,
                        )
                    )
                }
            }
        }
    }

    /**
     * The scope separator arrives percent-encoded, and the tarball url of the publisher must be
     * replaced by one pointing at this instance.
     */
    @Test
    fun testPackument() = testApplication {
        val mocks = Mocks()
        coEvery {
            mocks.getPackageByNameUseCase("@guimauve/fake-pkg", PackageFormat.NPM, null)
        } returns scopedPackage
        coEvery { mocks.listPackageVersionsUseCase(RoutesTestHelper.TEST_PACKAGE_ID) } returns listOf(version)
        configureApp(mocks)

        val response = client.get("/npm/@guimauve%2ffake-pkg")

        assertEquals(HttpStatusCode.OK, response.status)
        with(response.bodyAsText()) {
            assertContains(this, """"dist-tags":{"latest":"1.0.0"}""")
            assertContains(this, "/npm/@guimauve/fake-pkg/-/guimauve-fake-pkg-1.0.0.tgz")
            assertFalse(contains("elsewhere.example.com"), "the publisher url was served back")
            assertContains(this, """"shasum":"abc"""")
        }
    }

    @Test
    fun testUnknownPackument() = testApplication {
        val mocks = Mocks()
        coEvery { mocks.getPackageByNameUseCase(any(), any(), any()) } returns null
        configureApp(mocks)

        val response = client.get("/npm/@guimauve%2ffake-pkg")

        assertEquals(HttpStatusCode.NotFound, response.status)
        assertEquals("""{"error":"packages_not_found"}""", response.bodyAsText())
    }

    /**
     * The tarball is fetched with the scope spelled out, unlike the document.
     */
    @Test
    fun testTarball() = testApplication {
        val mocks = Mocks()
        coEvery {
            mocks.getPackageByNameUseCase("@guimauve/fake-pkg", PackageFormat.NPM, null)
        } returns scopedPackage
        coEvery {
            mocks.getLatestPackageVersionFileUseCase("guimauve-fake-pkg-1.0.0.tgz", RoutesTestHelper.TEST_PACKAGE_ID)
        } returns tarball
        coEvery { mocks.downloadFileUseCase(tarball) } returns "tgz!".toByteArray()
        configureApp(mocks)

        val response = client.get("/npm/@guimauve/fake-pkg/-/guimauve-fake-pkg-1.0.0.tgz")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("tgz!", response.bodyAsText())
    }

    @Test
    fun testVersionManifest() = testApplication {
        val mocks = Mocks()
        coEvery {
            mocks.getPackageByNameUseCase("@guimauve/fake-pkg", PackageFormat.NPM, null)
        } returns scopedPackage
        coEvery {
            mocks.getPackageVersionByNameUseCase("1.0.0", RoutesTestHelper.TEST_PACKAGE_ID)
        } returns version
        configureApp(mocks)

        val response = client.get("/npm/@guimauve/fake-pkg/1.0.0")

        assertEquals(HttpStatusCode.OK, response.status)
        assertContains(response.bodyAsText(), """"version":"1.0.0"""")
    }

    /**
     * What `npm login --auth-type=legacy` calls to obtain the token it writes to the `.npmrc`.
     */
    @Test
    fun testLegacyLogin() = testApplication {
        val mocks = Mocks()
        coEvery {
            mocks.loginUseCase(LoginPayload("someone@guimauve.digital", "hunter2"))
        } returns RoutesTestHelper.testUser
        every {
            mocks.tokenService.generateToken(RoutesTestHelper.TEST_USER_ID, TokenType.ACCESS)
        } returns "a-token"
        configureApp(mocks)

        val response = client.put("/npm/-/user/org.couchdb.user:someone") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":"someone@guimauve.digital","password":"hunter2"}""")
        }

        assertEquals(HttpStatusCode.Created, response.status)
        assertContains(response.bodyAsText(), """"token":"a-token"""")
    }

    @Test
    fun testLoginWithWrongCredentials() = testApplication {
        val mocks = Mocks()
        coEvery { mocks.loginUseCase(any()) } returns null
        configureApp(mocks)

        val response = client.put("/npm/-/user/org.couchdb.user:someone") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":"someone@guimauve.digital","password":"wrong"}""")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals("""{"error":"auth_invalid_credentials"}""", response.bodyAsText())
    }

    @Test
    fun testPublishAnonymously() = testApplication {
        val mocks = Mocks()
        configureApp(mocks)

        val response = client.put("/npm/@guimauve%2ffake-pkg") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":"@guimauve/fake-pkg","versions":{},"_attachments":{}}""")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals("""{"error":"auth_invalid_credentials"}""", response.bodyAsText())
    }

}
