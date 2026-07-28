package digital.guimauve.pkg.presentation.routes.packages.pypi

import digital.guimauve.pkg.domain.models.packages.PackageFormat
import digital.guimauve.pkg.domain.usecases.packages.GetPackageByNameUseCase
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
import kotlin.test.assertContains
import kotlin.test.assertEquals

class PyPiRoutesTest {

    private class Mocks {
        val getPackageByNameUseCase = mockk<GetPackageByNameUseCase>()
        val getUserUseCase = mockk<GetUserUseCase>()
    }

    private fun ApplicationTestBuilder.configureApp(mocks: Mocks) {
        application {
            configureTestApplication(mocks.getUserUseCase)
            routing {
                authenticate("api-jwt", optional = true) {
                    pypiRoutes(
                        PyPiRoutesDependencies(
                            getPackageByNameUseCase = mocks.getPackageByNameUseCase,
                            getUserUseCase = mocks.getUserUseCase,
                        )
                    )
                }
            }
        }
    }

    @Test
    fun testSimpleIndex() = testApplication {
        val mocks = Mocks()
        configureApp(mocks)

        val response = client.get("/pypi/simple")

        assertEquals(HttpStatusCode.OK, response.status)
        assertContains(response.bodyAsText(), "Simple index")
    }

    @Test
    fun testPackageIndex() = testApplication {
        val mocks = Mocks()
        coEvery { mocks.getPackageByNameUseCase("library", PackageFormat.PYPI, null) } returns
                RoutesTestHelper.testPackage
        configureApp(mocks)

        val response = client.get("/pypi/simple/library")

        assertEquals(HttpStatusCode.OK, response.status)
        assertContains(response.bodyAsText(), "Links for com.example:library")
    }

    @Test
    fun testUnknownPackageIndex() = testApplication {
        val mocks = Mocks()
        coEvery { mocks.getPackageByNameUseCase(any(), any(), any()) } returns null
        configureApp(mocks)

        val response = client.get("/pypi/simple/library")

        assertEquals(HttpStatusCode.NotFound, response.status)
        assertEquals("""{"error":"packages_not_found"}""", response.bodyAsText())
    }

    @Test
    fun testUploadAnonymously() = testApplication {
        val mocks = Mocks()
        configureApp(mocks)

        val response = client.post("/pypi")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals("""{"error":"auth_invalid_credentials"}""", response.bodyAsText())
    }

}
