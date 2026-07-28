package digital.guimauve.pkg.presentation.routes.packages.maven

import digital.guimauve.pkg.domain.exceptions.packages.PackagePrivateException
import digital.guimauve.pkg.domain.models.packages.PackageFormat
import digital.guimauve.pkg.domain.models.packages.versions.PackageVersion
import digital.guimauve.pkg.domain.models.packages.versions.files.PackageVersionFile
import digital.guimauve.pkg.domain.usecases.packages.GetOrCreatePackageUseCase
import digital.guimauve.pkg.domain.usecases.packages.GetPackageByNameUseCase
import digital.guimauve.pkg.domain.usecases.packages.maven.ParseMavenPathUseCaseImpl
import digital.guimauve.pkg.domain.usecases.packages.versions.GetLatestPackageVersionUseCase
import digital.guimauve.pkg.domain.usecases.packages.versions.GetOrCreatePackageVersionUseCase
import digital.guimauve.pkg.domain.usecases.packages.versions.GetPackageVersionByNameUseCase
import digital.guimauve.pkg.domain.usecases.packages.versions.files.CreatePackageVersionFileUseCase
import digital.guimauve.pkg.domain.usecases.packages.versions.files.DownloadFileUseCase
import digital.guimauve.pkg.domain.usecases.packages.versions.files.GetLatestPackageVersionFileUseCase
import digital.guimauve.pkg.domain.usecases.packages.versions.files.GetPackageVersionFileByNameUseCase
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
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant
import kotlin.uuid.Uuid

class MavenRoutesTest {

    private val artifactPath = "/maven2/com/example/library/1.0.0/library-1.0.0.jar"

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
        size = 4,
        path = "some/path/library-1.0.0.jar",
    )

    private class Mocks {
        val getPackageByNameUseCase = mockk<GetPackageByNameUseCase>()
        val getOrCreatePackageUseCase = mockk<GetOrCreatePackageUseCase>()
        val getPackageVersionByNameUseCase = mockk<GetPackageVersionByNameUseCase>()
        val getOrCreatePackageVersionUseCase = mockk<GetOrCreatePackageVersionUseCase>()
        val getLatestPackageVersionUseCase = mockk<GetLatestPackageVersionUseCase>()
        val getPackageVersionFileByNameUseCase = mockk<GetPackageVersionFileByNameUseCase>()
        val getLatestPackageVersionFileUseCase = mockk<GetLatestPackageVersionFileUseCase>()
        val createPackageVersionFileUseCase = mockk<CreatePackageVersionFileUseCase>()
        val downloadFileUseCase = mockk<DownloadFileUseCase>()
        val getUserUseCase = mockk<GetUserUseCase>()
    }

    private fun ApplicationTestBuilder.configureApp(mocks: Mocks) {
        application {
            configureTestApplication(mocks.getUserUseCase)
            routing {
                authenticate("api-jwt", optional = true) {
                    mavenRoutes(
                        MavenRoutesDependencies(
                            parseMavenPathUseCase = ParseMavenPathUseCaseImpl(),
                            getPackageByNameUseCase = mocks.getPackageByNameUseCase,
                            getOrCreatePackageUseCase = mocks.getOrCreatePackageUseCase,
                            getPackageVersionByNameUseCase = mocks.getPackageVersionByNameUseCase,
                            getOrCreatePackageVersionUseCase = mocks.getOrCreatePackageVersionUseCase,
                            getLatestPackageVersionUseCase = mocks.getLatestPackageVersionUseCase,
                            getPackageVersionFileByNameUseCase = mocks.getPackageVersionFileByNameUseCase,
                            getLatestPackageVersionFileUseCase = mocks.getLatestPackageVersionFileUseCase,
                            createPackageVersionFileUseCase = mocks.createPackageVersionFileUseCase,
                            downloadFileUseCase = mocks.downloadFileUseCase,
                            getUserUseCase = mocks.getUserUseCase,
                        )
                    )
                }
            }
        }
    }

    @Test
    fun testDownloadArtifact() = testApplication {
        val mocks = Mocks()
        coEvery { mocks.getPackageByNameUseCase("com.example:library", PackageFormat.MAVEN, null) } returns
                RoutesTestHelper.testPackage
        coEvery { mocks.getPackageVersionByNameUseCase("1.0.0", RoutesTestHelper.TEST_PACKAGE_ID) } returns version
        coEvery {
            mocks.getPackageVersionFileByNameUseCase("library-1.0.0.jar", RoutesTestHelper.TEST_VERSION_ID)
        } returns file
        coEvery { mocks.downloadFileUseCase(file) } returns "jar!".toByteArray()
        configureApp(mocks)

        val response = client.get(artifactPath)

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("application/java-archive", response.contentType()?.withoutParameters()?.toString())
        assertEquals("jar!", response.bodyAsText())
    }

    @Test
    fun testDownloadUnknownPackage() = testApplication {
        val mocks = Mocks()
        coEvery { mocks.getPackageByNameUseCase(any(), any(), any()) } returns null
        configureApp(mocks)

        val response = client.get(artifactPath)

        assertEquals(HttpStatusCode.NotFound, response.status)
        assertEquals("""{"error":"packages_not_found"}""", response.bodyAsText())
    }

    @Test
    fun testDownloadPrivatePackageAnonymously() = testApplication {
        val mocks = Mocks()
        coEvery { mocks.getPackageByNameUseCase(any(), any(), any()) } throws PackagePrivateException()
        configureApp(mocks)

        val response = client.get(artifactPath)

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals("""{"error":"packages_private"}""", response.bodyAsText())
    }

    @Test
    fun testDownloadWithTooShortPath() = testApplication {
        val mocks = Mocks()
        configureApp(mocks)

        val response = client.get("/maven2/com/example")

        assertEquals(HttpStatusCode.NotFound, response.status)
        assertEquals("""{"error":"invalid_path"}""", response.bodyAsText())
    }

    @Test
    fun testPublishArtifactAnonymously() = testApplication {
        val mocks = Mocks()
        coEvery { mocks.getUserUseCase(any()) } returns null
        configureApp(mocks)

        val response = client.put(artifactPath)

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals("""{"error":"auth_invalid_credentials"}""", response.bodyAsText())
    }

    @Test
    fun testPublishArtifact() = testApplication {
        val mocks = Mocks()
        coEvery { mocks.getUserUseCase(RoutesTestHelper.TEST_USER_ID) } returns RoutesTestHelper.testUser
        coEvery {
            mocks.getOrCreatePackageUseCase("com.example:library", PackageFormat.MAVEN, RoutesTestHelper.testUser)
        } returns RoutesTestHelper.testPackage
        coEvery {
            mocks.getOrCreatePackageVersionUseCase(
                "1.0.0",
                RoutesTestHelper.TEST_PACKAGE_ID,
                RoutesTestHelper.testUser
            )
        } returns version
        coEvery { mocks.createPackageVersionFileUseCase(any(), RoutesTestHelper.TEST_VERSION_ID, any()) } returns file
        configureApp(mocks)

        val response = client.put(artifactPath) {
            bearerAuth(RoutesTestHelper.generateTestToken())
            setBody("jar!".toByteArray())
        }

        assertEquals(HttpStatusCode.NoContent, response.status)
        coVerify { mocks.createPackageVersionFileUseCase(any(), RoutesTestHelper.TEST_VERSION_ID, any()) }
    }

}
