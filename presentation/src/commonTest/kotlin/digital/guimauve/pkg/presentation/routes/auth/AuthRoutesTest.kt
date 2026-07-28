package digital.guimauve.pkg.presentation.routes.auth

import digital.guimauve.pkg.domain.models.auth.LoginPayload
import digital.guimauve.pkg.domain.usecases.auth.LoginUseCase
import digital.guimauve.pkg.domain.usecases.users.GetUserUseCase
import digital.guimauve.pkg.presentation.routes.RoutesTestHelper
import digital.guimauve.pkg.presentation.routes.RoutesTestHelper.configureTestApplication
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
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

class AuthRoutesTest {

    private class Mocks {
        val loginUseCase = mockk<LoginUseCase>()
        val getUserUseCase = mockk<GetUserUseCase>()
    }

    private fun ApplicationTestBuilder.configureApp(mocks: Mocks) {
        application {
            configureTestApplication(mocks.getUserUseCase)
            routing {
                authenticate("api-jwt", optional = true) {
                    authRoutes(AuthRoutesDependencies(mocks.loginUseCase, mocks.getUserUseCase))
                }
            }
        }
    }

    /**
     * Renders through the layout. The test bundle echoes the keys, so this asserts which keys the
     * page asks for; that they resolve to English is covered by the application test.
     */
    @Test
    fun testLoginPage() = testApplication {
        val mocks = Mocks()
        configureApp(mocks)

        val response = client.get("/auth/login")

        assertEquals(HttpStatusCode.OK, response.status)
        with(response.bodyAsText()) {
            assertContains(this, "<title>auth_login_title — PKG</title>")
            assertContains(this, """<label for="email" class="form-label">auth_field_email</label>""")
        }
    }

    @Test
    fun testLogin() = testApplication {
        val mocks = Mocks()
        val payload = LoginPayload("test@guimauve.digital", "hunter2")
        coEvery { mocks.loginUseCase(payload) } returns RoutesTestHelper.testUser
        configureApp(mocks)

        val response = client.config { followRedirects = false }.submitForm(
            url = "/auth/login?redirect=/packages",
            formParameters = parameters {
                append("email", payload.email)
                append("password", payload.password)
            }
        )

        assertEquals(HttpStatusCode.Found, response.status)
        assertEquals("/packages", response.headers[HttpHeaders.Location])
        assertContains(response.headers[HttpHeaders.SetCookie].orEmpty(), "session")
    }

    @Test
    fun testLoginWithWrongCredentials() = testApplication {
        val mocks = Mocks()
        coEvery { mocks.loginUseCase(any()) } returns null
        configureApp(mocks)

        val response = client.submitForm(
            url = "/auth/login",
            formParameters = parameters {
                append("email", "test@guimauve.digital")
                append("password", "wrong")
            }
        )

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertContains(response.bodyAsText(), "auth_invalid_credentials")
    }

    /**
     * A `?redirect=` pointing at another host must not be honoured.
     */
    @Test
    fun testLogoutIgnoresForeignRedirect() = testApplication {
        val mocks = Mocks()
        configureApp(mocks)

        val response = client.config { followRedirects = false }.get("/auth/logout?redirect=//evil.example.com")

        assertEquals(HttpStatusCode.Found, response.status)
        assertEquals("/", response.headers[HttpHeaders.Location])
    }

}
