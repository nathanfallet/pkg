package digital.guimauve.pkg.presentation.routes.auth

import digital.guimauve.pkg.domain.models.auth.LoginPayload
import digital.guimauve.pkg.domain.usecases.auth.LoginUseCase
import digital.guimauve.pkg.domain.usecases.users.GetUserUseCase
import digital.guimauve.pkg.presentation.extensions.clearSession
import digital.guimauve.pkg.presentation.extensions.respondView
import digital.guimauve.pkg.presentation.extensions.setSession
import digital.guimauve.pkg.presentation.extensions.userOrNull
import digital.guimauve.pkg.presentation.mappers.users.toUserView
import digital.guimauve.pkg.presentation.models.SessionPayload
import digital.guimauve.pkg.presentation.views.LayoutView
import digital.guimauve.pkg.presentation.views.LoginPageView
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Dependencies required for authentication routes.
 */
data class AuthRoutesDependencies(
    val loginUseCase: LoginUseCase,
    val getUserUseCase: GetUserUseCase,
)

/**
 * Where to send the visitor once signed in or out. Only same-site paths are honoured, so that a
 * crafted `?redirect=` cannot bounce anyone to another host.
 */
private fun ApplicationCall.redirectTarget(): String =
    request.queryParameters["redirect"]?.takeIf { it.startsWith("/") && !it.startsWith("//") } ?: "/"

/**
 * Configures authentication routes.
 */
fun Route.authRoutes(dependencies: AuthRoutesDependencies) = with(dependencies) {
    get("/auth/login") {
        val layout = LayoutView("auth_login_title", call.userOrNull(getUserUseCase)?.toUserView())
        call.respondView("public/auth/login.ftl", LoginPageView(layout, error = null))
    }
    post("/auth/login") {
        val parameters = call.receiveParameters()
        val payload = LoginPayload(
            email = parameters["email"].orEmpty(),
            password = parameters["password"].orEmpty(),
        )
        val user = loginUseCase(payload) ?: run {
            val layout = LayoutView("auth_login_title", user = null)
            return@post call.respondView(
                "public/auth/login.ftl",
                LoginPageView(layout, error = "auth_invalid_credentials"),
                HttpStatusCode.Unauthorized,
            )
        }
        call.setSession(SessionPayload(user.id))
        call.respondRedirect(call.redirectTarget())
    }
    get("/auth/logout") {
        call.clearSession()
        call.respondRedirect(call.redirectTarget())
    }
}
