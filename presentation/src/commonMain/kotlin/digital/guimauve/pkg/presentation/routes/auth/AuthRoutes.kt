package digital.guimauve.pkg.presentation.routes.auth

import digital.guimauve.pkg.domain.models.auth.LoginPayload
import digital.guimauve.pkg.domain.models.users.User
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

private val EMAIL_REGEX = Regex(User.EMAIL_REGEX)

/**
 * Where to send the visitor once signed in or out. Only same-site paths are honoured, so that a
 * crafted `?redirect=` cannot bounce anyone to another host.
 *
 * A single path may not be enough to be safe: a browser strips tabs and newlines before parsing a
 * URL, and reads a backslash as a separator, so `/\host` and `/<tab>/host` both end up pointing at
 * `host`. Anything below a space is therefore refused outright before the shape is checked.
 */
private val SAME_SITE_PATH = Regex("""^/(?![/\\]).*""")

private fun ApplicationCall.redirectTarget(): String = request.queryParameters["redirect"]
    ?.takeIf { target -> target.none { it <= ' ' } && SAME_SITE_PATH.matches(target) }
    ?: "/"

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
        // Tell a malformed address apart from a wrong one, rather than sending both to the database.
        if (!EMAIL_REGEX.matches(payload.email)) {
            val layout = LayoutView("auth_login_title", user = null)
            return@post call.respondView(
                "public/auth/login.ftl",
                LoginPageView(layout, error = "auth_email_regex"),
                HttpStatusCode.BadRequest,
            )
        }
        val user = loginUseCase(payload) ?: run {
            val layout = LayoutView("auth_login_title", user = null)
            return@post call.respondView(
                "public/auth/login.ftl",
                LoginPageView(layout, error = "auth_invalid_credentials"),
                HttpStatusCode.Unauthorized,
            )
        }
        // Drop whatever session the visitor arrived with, so a planted id cannot survive the login
        // and end up pointing at the account that just signed in.
        call.clearSession()
        call.setSession(SessionPayload(user.id))
        call.respondRedirect(call.redirectTarget())
    }
    get("/auth/logout") {
        call.clearSession()
        call.respondRedirect(call.redirectTarget())
    }
}
